package ru.home.testcoreinit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import ru.home.processor.ITest;
import ru.home.processor.ImplTest;
import ru.home.processor.InitTest;


public class MainActivity extends AppCompatActivity {

    @InitTest
    ImplTest testData;
    Integer data2 = 5;
    String data;
    boolean var = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println(var);
    }
}
