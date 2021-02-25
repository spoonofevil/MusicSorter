package spoony.testopendirtree;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.Layout;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class PopupAddGenre extends Dialog {
    private Button addgenre;
    private TextView InputGenre;
    private TextView genreadded;
    private Button confirmaddedgenre;
    private LinearLayout genreajouter;

    public PopupAddGenre(Activity activity){
        super(activity,R.style.Widget_MaterialComponents_ActionBar_Solid);
        setContentView(R.layout.addgenrepopup);
        this.addgenre= findViewById(R.id.btnaddgenre);
        this.InputGenre=findViewById(R.id.inputgenretxt);
        this.confirmaddedgenre=findViewById(R.id.confirmaddedgenre);
        this.genreadded=findViewById(R.id.ListeGenreadded);
        this.genreajouter=findViewById(R.id.genreaajouter);
    }

    public Button getaddgenre() {
        return addgenre;
    }

    public TextView getInputGenre() {
        return InputGenre;
    }

    public LinearLayout getGenreajouter(){return genreajouter;}

    public Button getConfirmaddedgenre() {
        return confirmaddedgenre;
    }

    public TextView getGenreadded() {
        return genreadded;
    }
    public void build(){
        show();
    }
}
