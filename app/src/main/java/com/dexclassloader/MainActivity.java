package com.dexclassloader;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button button1;
    TextView textView1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1 = findViewById(R.id.button);
        button1.setOnClickListener(this);
        textView1 = findViewById(R.id.textView);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button) {
            performAction();
        }
    }

    private void performAction() {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        Future<String> future = pool.submit(new LoaderCallable(this.getApplicationContext()));
        try {
            String string = future.get();
            textView1.setText(string);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}

class LoaderCallable implements Callable<String> {

    private Context context;


    LoaderCallable(Context applicationContext) {
        this.context = applicationContext;
    }

    @Override
    public String call() throws Exception {

        Thread thread = new Thread(new DownloaderRunnable(context));
        thread.start();
        thread.join();

        DexClassLoader loader = new DexClassLoader(context.getFilesDir().getAbsolutePath().concat("/output.jar"),
                context.getDir("outdex", Context.MODE_PRIVATE).getAbsolutePath(),
                null, context.getClassLoader());

        Class<?> beanClass = loader.loadClass("ClassToLoadWithClassloader");

        // Create a new instance from the loaded class
        Constructor<?> constructor = beanClass.getConstructor();
        Object beanObj = constructor.newInstance();

        // Getting a method from the loaded class and invoke it
        Method method = beanClass.getMethod("sayHello");
        return (String) method.invoke(beanObj);
    }

}
