package spoony.testopendirtree;

import android.app.Activity;
import android.app.Dialog;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

public class PopupConfirmInfoMusic extends Dialog {

    private String NameChosen, AlbumChosen, GenreChosen,ArtistChosen;
    private TextView NameChosenTextview, AlbumChosenTextview, GenreChosenTextview,ArtistChosenTextview;
    private Button ConfirmPopup, CancelPopup;
    private File AlbumArt;

    public PopupConfirmInfoMusic(Activity activity){
        super(activity,R.style.Widget_MaterialComponents_ActionBar_Solid);
        setContentView(R.layout.popub_info_music);
        this.NameChosen="Song";
        this.AlbumChosen="Unknown Album";
        this.GenreChosen="Other";
        this.ArtistChosen="Unknown Artist";
        this.NameChosenTextview=findViewById(R.id.Namechosen);
        this.ArtistChosenTextview=findViewById(R.id.ArtistChosen);
        this.AlbumChosenTextview=findViewById(R.id.AlbumChosen);
        this.GenreChosenTextview=findViewById(R.id.GenreChosen);
        this.ConfirmPopup=findViewById(R.id.confirmPopub);
        this.CancelPopup=findViewById(R.id.exitPopub);
    }

    public void setNameChosen(String nameChosen){
        if(nameChosen!=null){
            this.NameChosen=nameChosen;
        }
    }
    public void setAlbumChosen(String albumChosen){
        if(albumChosen!=null) {
            this.AlbumChosen = albumChosen;
        }
    }
    public void setGenreChosen(String genreChosen){
        if(genreChosen!=null){
            this.GenreChosen=genreChosen;
        }
    }
    public void setArtistChosen(String artistChosen){
        if(artistChosen!=null){
            this.ArtistChosen=artistChosen;
        }
    }

    public Button getConfirmPopup(){return ConfirmPopup;}
    public Button getCancelPopup(){return CancelPopup;}

    public void build(){
        show();
        NameChosenTextview.setText(NameChosen);
        AlbumChosenTextview.setText(AlbumChosen);
        GenreChosenTextview.setText(GenreChosen);
        ArtistChosenTextview.setText(ArtistChosen);
    }

}
