package com.example.sign_language_recognition_app;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Recognize_Sign_Language extends AppCompatActivity {
    private static final int FROM_ALBUM = 1;    // onActivityResult 식별자
    private static final int FROM_CAMERA = 2;   // 카메라는 사용 안함

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recognize_sign_language);

        // 인텐트의 결과는 onActivityResult 함수에서 수신.
        // 여러 개의 인텐트를 동시에 사용하기 때문에 숫자를 통해 결과 식별(FROM_ALBUM 등등)
        findViewById(R.id.button_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");                      // 이미지만
                intent.setAction(Intent.ACTION_GET_CONTENT);    // 카메라(ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, FROM_ALBUM);
            }
        });
    }

    // 사진첩에서 사진 파일 불러와 분류하는 코드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        TextView tv_output = findViewById(R.id.tv_output);
        // 카메라를 다루지 않기 때문에 앨범 상수에 대해서 성공한 경우에 대해서만 처리
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != FROM_ALBUM || resultCode != RESULT_OK)
            return;

        //각 모델(.h5)에 따른 input , output shape 각자 맞게 변환
        // input: 모델의 placeholder에 전달할 데이터
        // output: 텐서플로 모델로부터 결과를 넘겨받을 배열. 덮어쓰기 때문에 초기값은 없다.
        float[][][][] input = new float[1][150][150][3];
        float[][] output = new float[1][9]; //tflite에 버섯 종류 2개라서 (내기준)

        try {
            int batchNum = 0;
            InputStream buf = getContentResolver().openInputStream(data.getData());
            Bitmap bitmap = BitmapFactory.decodeStream(buf);
            buf.close();

            //이미지 뷰에 선택한 사진 띄우기
            ImageView iv = findViewById(R.id.image);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            iv.setImageBitmap(bitmap);



            // x,y 최댓값 사진 크기에 따라 달라짐 (조절 해줘야함)
            for (int x = 0; x < 150; x++) {
                for (int y = 0; y < 150; y++) {
                    int pixel = bitmap.getPixel(x, y);
                    input[batchNum][x][y][0] = Color.red(pixel) / 1.0f;
                    input[batchNum][x][y][1] = Color.green(pixel) / 1.0f;
                    input[batchNum][x][y][2] = Color.blue(pixel) / 1.0f;
                }
            }

            // 모델을 해석할 인터프리터 생성 및 구동
            // 정확하게는 from_session 함수의 output_tensors 매개변수에 전달된 연산 호출
            Interpreter lite = getTfliteInterpreter("converted_model.tflite");
            lite.run(input, output);

//            tv_output.setText("output[0]: "+String.valueOf(output[0][0])+"\n"+"output[0][1]: "+String.valueOf(output[0][1])+"\n"+
//                    "output[0][2]:"+String.valueOf(output[0][2])+"\n"+"output[0][3]: "+String.valueOf(output[0][3])+"\n"+
//                    "output[0][4]: "+String.valueOf(output[0][4])+"\n"+"output[0][5]: "+String.valueOf(output[0][5])+"\n"+
//                    "output[0][6]: "+String.valueOf(output[0][6])+"\n"+"output[0][7]: "+String.valueOf(output[0][7])+"\n"+
//                    "output[0][8]: "+String.valueOf(output[0][8]));
//                    "input[0]: "+String.valueOf(input[0])+"\n"+
//                    "input[0][0]: "+String.valueOf(input[0][0])+"\n"+
//                    "input[0][0][0]: "+String.valueOf(input[0][0][0])+"\n"+
//                    "input[0][0][0][0]: "+String.valueOf(input[0][0][0][0])+"\n"+
//                    "input[0][0][0][1]: "+String.valueOf(input[0][0][0][1])+"\n"+
//                    "input[0][0][0][2]: "+String.valueOf(input[0][0][0][2]));

        } catch (IOException e) {
            e.printStackTrace();
        }

        int i;
        // 텍스트뷰에 분류표기
        for (i = 0; i < 9; i++) {
            if (output[0][i] * 100 > 80) {
                if (i == 0) {
                    tv_output.setText(String.format("개나리 광대버섯  %d %.5f", i, output[0][0] * 100));
                } else if (i == 1) {
                    tv_output.setText(String.format("붉은사슴뿔버섯,%d  %.5f", i, output[0][1] * 100));
                } else if (i == 2) {
                    tv_output.setText(String.format("새송이버섯,%d, %.5f", i, output[0][2] * 100));
                } else if (i == 3) {
                    tv_output.setText(String.format("표고버섯, %d, %.5f", i, output[0][3] * 100));
                } else if (i == 4) {
                    tv_output.setText(String.format("4, %d, %.5f", i, output[0][4] * 100));
                } else if (i == 5) {
                    tv_output.setText(String.format("5, %d, %.5f", i, output[0][5] * 100));
                } else if (i == 6) {
                    tv_output.setText(String.format("6, %d, %.5f", i, output[0][6] * 100));
                } else if (i == 7) {
                    tv_output.setText(String.format("7, %d, %.5f", i, output[0][7] * 100));
                } else if (i == 8) {
                    tv_output.setText(String.format("8, %d, %.5f", i, output[0][8] * 100));
                } else {
                    tv_output.setText(String.format("9, %d, %.5f", i, output[0][9] * 100));
                }
            } else
                continue;
        }
    }





    // 모델 파일 인터프리터를 생성하는 공통 함수
    // loadModelFile 함수에 예외가 포함되어 있기 때문에 반드시 try, catch 블록이 필요하다.
    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(Recognize_Sign_Language.this, modelPath));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    // 모델 읽는 함수. MappedByteBuffer 바이트 버퍼를 Interpreter객체에 전달하면 모델해석 가능
    public MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

}

