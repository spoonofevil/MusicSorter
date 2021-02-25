package spoony.testopendirtree;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

//ceci n'est qu'un test de l'appel de la fonction ACTION_GET_CONTENT (qui ne me convient pas car elle renvoie un fichier et non un dossier)
public class Testopendir2 extends AppCompatActivity {

    TextView Txt_path;
    Button btnchooserpath;
    Intent myintent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testopendir2);

        Txt_path = findViewById(R.id.textdir2);
        btnchooserpath=findViewById(R.id.btnchooser);

        btnchooserpath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            myintent = new Intent(Intent.ACTION_GET_CONTENT);
            myintent.setType("*/*");
            startActivityForResult(myintent,10);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 10:
                    if(resultCode==RESULT_OK);
                    String Path = data.getData().getPath();
                    Txt_path.setText(Path);
                break;

        }
    }
}