package spoony.testopendirtree;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

//la lib
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public class AffichName extends AppCompatActivity {

    private AffichName acctivity;

    //XML components :
    private MediaPlayer actualMusic;
    LinearLayout layutlist;
    SeekBar progressMus,volume;
    TextView rremainTIme,elapsTime,Genrechoisi, Dir, Music;
    ImageView albumart;
    Button clearTrackName,clearAlbum, clearGenre, clearArtist,confirmInfo,nextMus,play,addgenre;
    public AutoCompleteTextView TextArtist, TextTrackname, TectAlbum;
    private ExpandableListView MusicGenre;
    //lié a la expendable view :
    List<String> ListGroup;
    List<String> musigGenreList = null;
    HashMap<String,List<String>> ListItem;
    MainAdapter adapter;

    //autre variables
    int indicegenretoadd,MusicTime;
    String dir = "DIR";
    String musicchoisi;
    int numero_list;

    //pour avoir la list des chanson (music) du dossier choisi au début
    Bundle b = null;
    ArrayList music = new ArrayList() ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

/**
 * Checks if the app has permission to write to device storage
 *
 * If the app does not has permission then the user will be prompted to grant permissions
 *
 * @param activity
 */
        /*--------------------for recup les genre de la save---------------
        SharedPreferences SP = getSharedPreferences("PREF",0);
        String listinstring = SP.getString("Genres","");
        StringBuilder SB = new StringBuilder();
        if(!listinstring.equals("") | listinstring!=null){
            SB.append(listinstring);
        }else{
            Toast.makeText(AffichName.this, "ERROR : no genre saved", Toast.LENGTH_SHORT).show();
        }
        for(String g : ListGenres){

        }

         */

        super.onCreate(savedInstanceState);
        this.acctivity = this;
        setContentView(R.layout.affich_dir);

        //setting the IDs
        this.albumart=findViewById(R.id.albumartview); //l'image de la musique
        this.Dir = findViewById(R.id.Text_Dir); //le dossier ou elle est
        this.layutlist=findViewById(R.id.layoutliste);
        this.TextTrackname = findViewById(R.id.TextName);
        this.TectAlbum = findViewById(R.id.TextAlbum);
        this.TextArtist = findViewById(R.id.ArtistName);
        this.MusicGenre = findViewById(R.id.Musicgenre);
        this.Music = findViewById(R.id.Text_Random_Mucis); //le nom de la musique prise au hasard
        this.elapsTime=findViewById(R.id.elapsTime);
        this.rremainTIme=findViewById(R.id.remainTime);


        //recupere le bundle de la Mainactivity (la list des chanson dans le dossier choisi
        Bundle b = getIntent().getExtras();
        music = b.getStringArrayList("content");

        //pour mettre les genres dans une listes
        ListItem = new HashMap<>();
        ListGroup = new ArrayList<>();
        adapter = new MainAdapter(this,ListGroup,ListItem);
        MusicGenre.setAdapter(adapter);
        InitListData();

        //pour rajouter un genre qd le user appuie dessus
        this.Genrechoisi = findViewById(R.id.Genrechoisitext);
        MusicGenre.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String selected = ListItem.get(ListGroup.get(0)).get(childPosition);
                System.out.println("le selectioné : "+selected);
                if(!TextUtils.isEmpty(selected) || !selected.equals(" ")){
                    if (TextUtils.isEmpty(Genrechoisi.getText())){
                        Genrechoisi.setText(selected);
                    }else{
                        Genrechoisi.setText(Genrechoisi.getText()+"/"+selected);
                    }
                }else{
                    Toast.makeText(AffichName.this, "No genre in save...", Toast.LENGTH_SHORT).show();
                    Popuaddgenrebuilder();
                }

                return true;
            }


        });

        //pour ajouter un genre a la liste
        this.addgenre = findViewById(R.id.addgenrebutton);
        addgenre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Popuaddgenrebuilder();
            }
        });

        this.play=findViewById(R.id.playbtn);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            playbtnclick(v);
            }
        });

        //pour changer la position de la musique en fonction de la seekbar
        this.progressMus=findViewById(R.id.progressBarMusic);
        progressMus.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    actualMusic.seekTo(progress);
                    progressMus.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //changer le volume
        this.volume=findViewById(R.id.volume);
        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Float volumef = progress/100f;
                actualMusic.setVolume(volumef,volumef);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //le bouton pour changer de musique
        this.nextMus = findViewById(R.id.nextMus);
        nextMus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextmus();
            }
        });

        //si il y a pas d'erreur sinon retour au menu pour avoir le bundle rempli (sert a rien)
        if (b != null) {
            dir = b.getString("Dir");
        }else { PopupEndOfSort();}
        Dir.setText("in : " + dir);


        /*try {
            actualMusic.setDataSource(dir+musicchoisi);
        } catch (IOException e) {
            System.out.println("erreur 1");
            e.printStackTrace();
        }
        actualMusic.start();

        mpintro = MediaPlayer.create(this, Uri.parse(musicchoisi));
        mpintro.setLooping(true);
        mpintro.start();

         */

        try {
            playmus(TectAlbum,TextArtist,TextTrackname,Genrechoisi);
        } catch (IOException | ReadOnlyFileException | TagException | InvalidAudioFrameException e) {
            e.printStackTrace();
        } catch (CannotReadException e) {
            e.printStackTrace();
        }

        //bouton pour clear les differents info donné par l'user
        this.clearTrackName = findViewById(R.id.clear_Trackname);
        clearTrackName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextTrackname.setText("");
//                TectAlbum.setHint(TectAlbum.getHint());
            }
        });
        this.clearArtist = findViewById(R.id.clear_ArtistName);
        clearArtist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextArtist.setText("");
//                TectAlbum.setHint(TectAlbum.getHint());
            }
        });
        this.clearAlbum = findViewById(R.id.clear_album);
        clearAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TectAlbum.setText("");
//                TectAlbum.setHint(TectAlbum.getHint());
            }
        });
        this.clearGenre = findViewById(R.id.clear_genre);
        clearGenre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Genrechoisi.setText("");
//                TectAlbum.setHint(TectAlbum.getHint());
            }
        });

        //valide les informations en ouvrant une fenetre popup mettant les infos qui vont etre changé pour les confirmer
        this.confirmInfo = findViewById(R.id.confirmInfo);
        confirmInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actualMusic.isPlaying()){
                    actualMusic.stop();
                }

                //recup les infos
                play.setBackgroundResource(R.drawable.play);
                String newalbum=TectAlbum.getText().toString();
                String newArtist=TextArtist.getText().toString();
                String newGenre=Genrechoisi.getText().toString();
                String newTittle=TextTrackname.getText().toString();

                //set all info of the popup
                PopupConfirmInfoMusic Popub = new PopupConfirmInfoMusic(acctivity);
                Popub.setNameChosen(newTittle);
                Popub.setAlbumChosen(newalbum);
                Popub.setGenreChosen(newGenre);
                Popub.setArtistChosen(newArtist);
                Popub.getConfirmPopup().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        File music_actuelFile = new File(dir+"/"+musicchoisi);
                        System.out.println("changing metadat from "+dir+"/"+musicchoisi);
                        try {
                            AudioFile musicchanged = AudioFileIO.read(music_actuelFile);
                            if(musicchanged==null){
                                Toast.makeText(AffichName.this, "file null", Toast.LENGTH_SHORT).show();
                            }else {
                                Tag newtag = musicchanged.getTag();
                                if (musicchanged.getTag() == null) {

                                    //UPDATE A FAIRE : faire les tags si il y en a pas
                                    Toast.makeText(AffichName.this, "PAS de TAG", Toast.LENGTH_SHORT).show();
                                } else {
                                    newtag.setField(FieldKey.ARTIST,newArtist);
                                    newtag.setField(FieldKey.ALBUM,newalbum);
                                    newtag.setField(FieldKey.TITLE,newTittle);
                                    newtag.setField(FieldKey.GENRE,newGenre);
                                    //Toast.makeText(AffichName.this, "artist : " + newtag.getFirst(FieldKey.ALBUM) + "\nArtist : " + tag.getFirst(FieldKey.ARTIST) + "\n Tittle : " + tag.getFirst(FieldKey.TITLE) + "\nYEAR ? : " + tag.getFirst(FieldKey.YEAR), Toast.LENGTH_SHORT).show();

                                    //met a jour les metadata
                                    if (shouldAskPermissions()) {
                                        askPermissions();
                                    }
                                    AudioFileIO.write(musicchanged);
                                   Toast.makeText(AffichName.this, "File saved at "+music_actuelFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException | CannotWriteException e) {
                            e.printStackTrace();
                        }
                        Popub.dismiss();
                        System.out.println("la musique numero "+numero_list+" du nom de "+music.get(numero_list)+" vient d'ettre enlevé");

                        //pour pas la retrier après coups
                        music.remove(numero_list);

                        //reset le media player
                        actualMusic.reset();

                        //reset les autocompletetextview
                        TextArtist.setText("");
                        TextTrackname.setText("");
                        TectAlbum.setText("");
                        Genrechoisi.setText("");
                        try {
                            playmus(TectAlbum,TextArtist,TextTrackname,Genrechoisi);
                        } catch (IOException | ReadOnlyFileException | TagException | InvalidAudioFrameException e) {
                            e.printStackTrace();
                        } catch (CannotReadException e) {
                            e.printStackTrace();
                        }
                    }
                });
                Popub.getCancelPopup().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(AffichName.this, "Operation cancelled", Toast.LENGTH_SHORT).show();
                        Popub.dismiss();
                    }
                });
                Popub.build();
                /* Version sans le popop custom (peut etre utile)
                AlertDialog.Builder InfoPopupConfirm = new AlertDialog.Builder(acctivityPopupInfoConfirm);
                InfoPopupConfirm.setTitle("Confirm Info");
                InfoPopupConfirm.setMessage("METADATA : \nName : "+TextTrackname.getText()+"\nAlbum : "+TectAlbum.getText()+"\nGenre : "+Genrechoisi.getText()+"\n\nAre you OK with that ?");
                InfoPopupConfirm.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(AffichName.this, "Changing metadata...", Toast.LENGTH_SHORT).show();
                    }
                });
                InfoPopupConfirm.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(AffichName.this, "Operation cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
                InfoPopupConfirm.show();
                 */
            }
        });
    }

    //prends les info sauvegardé dans les SHaredPreference et les mets dans la expandable listview
    private void InitListData() {

        //si il y avait plusieurs type de genres
        this.ListGroup.add("Genre");

        //Load la save des list
        SharedPreferences SP = getSharedPreferences("PREF",0);
        String listinstring = SP.getString("Genres","");

        String[] Array;
        musigGenreList = new ArrayList<>();

        //convertie la save de string à array avec comme separateur la virgule
        Array=listinstring.split(",");
        for(String genr : Array){
            musigGenreList.add(genr);
        }

        /* avant la save c'etait comme ca qu'on rajouter "a la main" les genre
        musgenres.add("Rock");
        musgenres.add("electro");
        musgenres.add("alternatif");
        musgenres.add("Rock alternatif");
        musgenres.add("jazz");
        musgenres.add("autre");
        for (String genre : Genre.txt){
        }
         */

        //met la liste fraichement builde dans la expandableLV
        this.ListItem.put(ListGroup.get(0),musigGenreList);
        this.adapter.notifyDataSetChanged();
        setListViewHeight(MusicGenre);
    }

    //mets a jour cette derniere avec une liste passé en parametre sous forme de string
    private void UpdateData(String genreupdate) {
        //String[] array;
        musigGenreList = new ArrayList<>();

        //convertie le String donné en argument de string à array avec comme separateur la virgule
        for(String genr : genreupdate.split(",")) {
            musigGenreList.add(genr);
        }

        //met la liste fraichement builde dans la expandableLV
        this.ListItem.put(ListGroup.get(0),musigGenreList);
        this.adapter.notifyDataSetChanged();
        setListViewHeight(MusicGenre);
    }

    //mets a jour le temps de la musique
    @SuppressLint("HandlerLeak")
    private final Handler handler= new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int Currentpos = msg.what;
            progressMus.setProgress(Currentpos);

            //met a jour les infos sur le changement de temps
            String elapsTim = createTimeLabel(Currentpos);
            elapsTime.setText(elapsTim);
            rremainTIme.setText("- "+createTimeLabel(MusicTime-Currentpos));
        }
    };

    //convertie un temps en miliseconde en string (pour de la musique)
    public String createTimeLabel(int tiem){
      String label ="";
      int min = tiem /1000 / 60;
      int sec = tiem / 1000 % 60;
      label=min+":";

      //pour pas avoir 1:3 quand c'est a 1 min et 3 seconde
      if (sec<10) label+="0";
      label+=sec;
      return label;
    };

    //crée le popup pour ajouter un ou plusieurs genres a la liste
    public void Popuaddgenrebuilder() {

        //recup la liste deja save
        SharedPreferences SP = getSharedPreferences("PREF",0);
        String listinstring = SP.getString("Genres","");
        System.out.println("la liste deja save :"+listinstring);

        //evite les probleme avec une liste finissant par une virgule (on sait jamais)
        if(listinstring.endsWith(",")) listinstring=listinstring.substring(0,listinstring.length()-1);

        PopupAddGenre Popubaddgenre = new PopupAddGenre(acctivity);

        Popubaddgenre.getGenreadded().setText(listinstring); //print la list deja save
        //puis la met sous forme de textviews pour pouvoir enlever un ou plusieurs elements lorsqu'il sont touchés
        for(String genredejaadd : listinstring.split(",")){
            TextView genretoadTextview=new TextView(AffichName.this);
            genretoadTextview.setText(genredejaadd);
            genretoadTextview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    indicegenretoadd=0;
                    System.out.println("on enleve "+genretoadTextview.getText());

                    //passer par un for evite d'enlevé toute les occurences d'un meme genre (et se retrouver avec une textview qui n'a pas de correspondance avec la liste print en bas et celle sauvergardé)
                    for(String identik : musigGenreList){
                        //System.out.println("num "+indicegenretoadd+ "/"+identik+"/");
                        if(identik.equals(genretoadTextview.getText().toString())){
                            //System.out.println(indicegenretoadd+"="+musigGenreList.get(indicegenretoadd));
                            musigGenreList.remove(indicegenretoadd);
                            break;
                        }
                        indicegenretoadd++;
                    }
                    //musigGenreList.remove(genretoadTextview.getText());

                    //permet de mettra a jour en buildant une string qui sera affichée et stockée (a la fin) dans la save (et affiché dans la expandable view bien sur)
                    StringBuilder SB2 = new StringBuilder();
                    for (String genress : musigGenreList){
                        SB2.append(genress+",");
                    }

                    //enleve si il ya un ou plusieurs virgule a la fin (il y en a tjr au moins une)
                    while(SB2.toString().endsWith(",")){
                        SB2.deleteCharAt(SB2.length()-1);
                    }
                    System.out.println("la liste "+SB2.toString());

                    Popubaddgenre.getGenreajouter().removeView(genretoadTextview);
                    Popubaddgenre.getGenreadded().setText(SB2);
                }
            });
            Popubaddgenre.getGenreajouter().addView(genretoadTextview);
        }
        Popubaddgenre.getConfirmaddedgenre().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //save the genre list in a file
                Toast.makeText(AffichName.this, "Changing saved genre list...", Toast.LENGTH_SHORT).show();
                SharedPreferences SP = getSharedPreferences("PREF",0);
                SharedPreferences.Editor editor = SP.edit();

                //pour pouvoir save il faut "construire" une string
                StringBuilder SB = new StringBuilder();
                System.out.println("la liste a update "+musigGenreList);
                for(String g : musigGenreList){
                    SB.append(g+",");
                    //System.out.println("genre : "+g);
                }
                SB.deleteCharAt(SB.length()-1); //enleve la virgule du dernier genre add
                System.out.println("new listgenre "+SB);

                //save la liste dans le SharedPreference
                editor.putString("Genres",SB.toString());
                editor.apply();
                UpdateData(SB.toString()); // met a jour la liste
                Popubaddgenre.dismiss();
            }
        });
        Popubaddgenre.getaddgenre().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //si le user a rien mit ca fait rien
                if(Popubaddgenre.getInputGenre().getText()=="" || TextUtils.isEmpty(Popubaddgenre.getInputGenre().getText()) || Popubaddgenre.getInputGenre().getText()==" "){
                    Toast.makeText(AffichName.this, "please enter a genre", Toast.LENGTH_SHORT).show();
                }else{
                    if(TextUtils.isEmpty(Popubaddgenre.getGenreadded().getText())) Popubaddgenre.getGenreadded().setText(Popubaddgenre.getInputGenre().getText()); // si il ya pas d'autre genre ca met pas la virgule avant
                    else Popubaddgenre.getGenreadded().setText(Popubaddgenre.getGenreadded().getText()+","+Popubaddgenre.getInputGenre().getText());
                    System.out.println("c la normalement :"+Popubaddgenre.getGenreadded().getText()+".");

                    musigGenreList.add(Popubaddgenre.getInputGenre().getText()+"");//ajoute le genre mis par le user dans la liste
                    TextView genretoadTextview=new TextView(AffichName.this);//rajoute le textview pour l'enlever si le user change d'avis
                    //ViewGroup.LayoutParams params = new ActionBar.LayoutParams();
                    genretoadTextview.setText(Popubaddgenre.getInputGenre().getText());

                    //pour l'enlever de la liste ( meme chose que precedement)
                    genretoadTextview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            indicegenretoadd=0;
                            System.out.println("on enleve "+genretoadTextview.getText());
                            for(String identik : musigGenreList){
                                //System.out.println("num "+indicegenretoadd+ "/"+identik+"/");
                                if(identik.equals(genretoadTextview.getText().toString())){
                                    //System.out.println(indicegenretoadd+"="+musigGenreList.get(indicegenretoadd));
                                    musigGenreList.remove(indicegenretoadd);
                                    break;
                                }
                                indicegenretoadd++;
                            }
                            //musigGenreList.remove(genretoadTextview.getText());
                            StringBuilder SB2 = new StringBuilder();
                            for (String genress : musigGenreList){
                                SB2.append(genress+",");
                            }
                            while(SB2.toString().endsWith(",")){
                                SB2.deleteCharAt(SB2.length()-1);
                            }
                            System.out.println("la liste "+SB2.toString());
                            Popubaddgenre.getGenreajouter().removeView(genretoadTextview);
                            Popubaddgenre.getGenreadded().setText(SB2);
                        }
                    });
                    Popubaddgenre.getGenreajouter().addView(genretoadTextview);
                    Popubaddgenre.getInputGenre().setText("");
                }
            }
        });
        Popubaddgenre.build();
    }
    public void playbtnclick(View view){
        if(!actualMusic.isPlaying()){
            actualMusic.start();
            this.play.setBackgroundResource(R.drawable.stop);
        }else {
            actualMusic.pause();
            try{
                this.play.setBackgroundResource(R.drawable.play);
            }catch (IllegalStateException e){
                e.printStackTrace();
            }
        }
    }

    //pour demander les perms
    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
        System.out.println("demande demandée");
        //Toast.makeText(this, "demande demandée", Toast.LENGTH_SHORT).show();
    }

    public void playmus(AutoCompleteTextView tectAlbum, AutoCompleteTextView textArtist, AutoCompleteTextView textTrack, TextView genretext) throws IOException, TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException {

        //si il a pas trouver de fichier (on sait jamais)
        if (music == null ){
            //revient au menu
            Intent menu = new Intent(getApplicationContext(), RealMain.class);
            startActivity(menu);
            finish();
        } else {
            //si la liste est vide (le tri est fini)
            if(music.isEmpty()){
                PopupEndOfSort();
            }else {
                //tire au hasard une music dans la liste
                Random r = new Random();
                numero_list = r.nextInt(music.size());
                System.out.println(numero_list);
                musicchoisi = (String) music.get(numero_list);
                System.out.println("in directory : " + dir);
                if (shouldAskPermissions()) {
                    askPermissions();
                }

                Music.setText(musicchoisi);
                File music_actuelFile = new File(dir + "/" + musicchoisi);
                System.out.println(dir + "/" + musicchoisi);
                try {
                    //créer un fichier audio (merci la lib) pour recup les tags
                    AudioFile f = AudioFileIO.read(music_actuelFile);
                    if(f==null){ //c pas censé arriver mais on sait jamais
                        Toast.makeText(this, "file null", Toast.LENGTH_SHORT).show();
                        nextmus();
                    }else {
                        //recup les tags (des metadata)
                        Tag tag = f.getTag();
                        if (f.getTag() == null) {
                            Toast.makeText(AffichName.this, "PAS de TAG", Toast.LENGTH_SHORT).show();
                        } else {
                            //pour recup l'album cover si il y en a une
                            if(tag.getFirstArtwork()!=null) {
                                byte[] picData = tag.getFirstArtwork().getBinaryData();
                                Bitmap bitmap = BitmapFactory.decodeByteArray(picData, 0, picData.length);
                                albumart.setImageBitmap(bitmap);
                            }else{ //sinon ca remet l'image de base
                                albumart.setImageResource(R.drawable.image);
                            }
                            //prends les tags et les montre a l'utilisateur dans les textBox
                            String artist= tag.getFirst(FieldKey.ARTIST);
                            String album= tag.getFirst(FieldKey.ALBUM);
                            String title = tag.getFirst(FieldKey.TITLE);
                            String genre = tag.getFirst(FieldKey.GENRE);
                            System.out.println("album : " + album + "\nArtist : " + artist + "\n Tittle : " + title + "\nGenre: " + genre + "\nYEAR ? : " + tag.getFirst(FieldKey.YEAR));
                            //Toast.makeText(AffichName.this, "artist : " + tag.getFirst(FieldKey.ALBUM) + "\nArtist : " + tag.getFirst(FieldKey.ARTIST) + "\n Tittle : " + tag.getFirst(FieldKey.TITLE) + "\nYEAR ? : " + tag.getFirst(FieldKey.YEAR), Toast.LENGTH_SHORT).show();
                            tectAlbum.setText(album);
                            textTrack.setText(title);
                            textArtist.setText(artist);
                            genretext.setText(genre);
                        }
                    }
                } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
                    e.printStackTrace();
                }

       /* c'etait avec la lib de mpatrick : mp3agic
        try {
            mp3file = new Mp3File(music_actuelFile);
            if (mp3file.hasId3v1Tag()) {
                ID3v1 mp3ID3V1tag = mp3file.getId3v1Tag();
                System.out.println("album : " + mp3ID3V1tag.getAlbum());
                System.out.println("trackname : " + mp3ID3V1tag.getTitle());
                System.out.println("artist : " + mp3ID3V1tag.getArtist());
                System.out.println("genre : " + mp3ID3V1tag.getGenreDescription());
                tectAlbum.setText(mp3ID3V1tag.getAlbum());
                textTrack.setText(mp3ID3V1tag.getTitle());
                textArtist.setText(mp3ID3V1tag.getArtist());
                genretext.setText(mp3ID3V1tag.getGenreDescription());
            } else if (mp3file.hasId3v2Tag()) {
                ID3v2 mp3ID3V2tag = mp3file.getId3v2Tag();
                System.out.println("album : " + mp3ID3V2tag.getAlbum());
                System.out.println("trackname : " + mp3ID3V2tag.getTitle());
                System.out.println("artist : " + mp3ID3V2tag.getArtist());
                System.out.println("genre : " + mp3ID3V2tag.getGenreDescription());
                tectAlbum.setText(mp3ID3V2tag.getAlbum());
                textTrack.setText(mp3ID3V2tag.getTitle());
                textArtist.setText(mp3ID3V2tag.getArtist());
                genretext.setText(mp3ID3V2tag.getGenreDescription());
            }
            if (mp3file.hasId3v1Tag() && mp3file.hasId3v2Tag()) {
                ID3v1 mp3ID3V1tag = mp3file.getId3v1Tag();
                ID3v2 mp3ID3V2tag = mp3file.getId3v2Tag();
                System.out.println("V2//V1: ");
                System.out.println("album : " + mp3ID3V2tag.getAlbum()+"//"+mp3ID3V1tag.getAlbum());
                System.out.println("trackname : " + mp3ID3V2tag.getTitle()+"//"+mp3ID3V1tag.getTitle());
                System.out.println("artist : " + mp3ID3V2tag.getArtist()+"//"+mp3ID3V1tag.getArtist());
                System.out.println("genre : " + mp3ID3V2tag.getGenreDescription()+"//"+mp3ID3V1tag.getGenreDescription());
                if (mp3ID3V2tag.getAlbum() == null) {
                    tectAlbum.setText(mp3ID3V1tag.getAlbum());
                } else if (mp3ID3V2tag.getAlbum().equals("")) {
                    tectAlbum.setText(mp3ID3V1tag.getAlbum());
                } else if (mp3ID3V2tag.getAlbum() != null) {
                    tectAlbum.setText(mp3ID3V2tag.getAlbum());
                }
                if (mp3ID3V2tag.getArtist() == null) {
                    textArtist.setText(mp3ID3V1tag.getArtist());
                } else if (mp3ID3V2tag.getArtist().equals("")) {
                    textArtist.setText(mp3ID3V1tag.getArtist());
                } else if (mp3ID3V2tag.getArtist() != null) {
                    textArtist.setText(mp3ID3V2tag.getArtist());
                }
                if (mp3ID3V2tag.getTitle() == null) {
                    tectAlbum.setText(mp3ID3V1tag.getTitle());
                } else if (mp3ID3V2tag.getTitle().equals("")) {
                    tectAlbum.setText(mp3ID3V1tag.getTitle());
                } else if (mp3ID3V2tag.getTitle() != null) {
                    tectAlbum.setText(mp3ID3V2tag.getTitle());
                }
                if (mp3ID3V2tag.getGenreDescription() == null) {
                    tectAlbum.setText(mp3ID3V1tag.getGenreDescription());
                } else if (mp3ID3V2tag.getGenreDescription().equals("")) {
                    tectAlbum.setText(mp3ID3V1tag.getGenreDescription());
                } else if (mp3ID3V2tag.getGenreDescription() != null) {
                    tectAlbum.setText(mp3ID3V2tag.getGenreDescription());
                }

            }
        } catch (UnsupportedTagException | InvalidDataException e) {
            e.printStackTrace();
        }
    System.out.println("Length of this mp3 is: " + mp3file.getLengthInSeconds() + " seconds");
    System.out.println("Bitrate: " + mp3file.getBitrate() + " kbps " + (mp3file.isVbr() ? "(VBR)" : "(CBR)"));
    System.out.println("Sample rate: " + mp3file.getSampleRate() + " Hz");
    System.out.println("Has ID3v1 tag?: " + (mp3file.hasId3v1Tag() ? "YES" : "NO"));
    System.out.println("Has ID3v2 tag?: " + (mp3file.hasId3v2Tag() ? "YES" : "NO"));
    System.out.println("Has custom tag?: " + (mp3file.hasCustomTag() ? "YES" : "NO"));


  */

                System.out.println("music : " + musicchoisi);

                //charge le fichier audio dans le mediaplayer et prepare le MediaPlayer
                this.actualMusic = MediaPlayer.create(this, Uri.fromFile(music_actuelFile));
                actualMusic.setLooping(true);
                actualMusic.seekTo(0);
                actualMusic.setVolume(0.5f, 0.5f);
                MusicTime = actualMusic.getDuration();
                progressMus.setMax(MusicTime); //met le max de la seekbar de progression de la musique a sa durée

                //a chaque seconde ca met a jour tt le coté lecture de la musique
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (actualMusic != null) {
                            try {
                                Message msg = new Message();
                                msg.what = actualMusic.getCurrentPosition();
                                handler.sendMessage(msg);
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        }
    }

    //met a jour le MP avec la nouvelle musique
    public void nextmus(){
        if(actualMusic.isPlaying()){
            actualMusic.stop();
        }
        play.setBackgroundResource(R.drawable.play);
        actualMusic.reset();

        //si la musique d'après n'a pas de metadata permet de clear les infos
        TextTrackname.setText("");
        TectAlbum.setText("");
        Genrechoisi.setText("");
        TextArtist.setText("");
        try {
            playmus(TectAlbum,TextArtist,TextTrackname,Genrechoisi);
        } catch (IOException | CannotReadException | ReadOnlyFileException | TagException | InvalidAudioFrameException e) {
            e.printStackTrace();
        }

    }

    //met un popup (default type) qui demande si le user continue le tri (si oui direction le menu) ou stop l'application
    public void PopupEndOfSort(){
        actualMusic.stop();
        actualMusic.reset();
        AlertDialog.Builder InfoPopupConfirm = new AlertDialog.Builder(acctivity);
        InfoPopupConfirm.setTitle("END OF SORTING");
        InfoPopupConfirm.setMessage("You sorted every song in the folder "+dir+"\n\nYou can now choose another one or quit the app");
        InfoPopupConfirm.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(AffichName.this, "Let's continue !", Toast.LENGTH_SHORT).show();
                GotoMenu();
            }
        });
        InfoPopupConfirm.setNegativeButton("QUIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(AffichName.this, "Exiting the app...", Toast.LENGTH_SHORT).show();
                finish();
                System.exit(0); //eteint l'app
            }
        });
        InfoPopupConfirm.show();
    }

    //va au menu niplunimoin
    private void GotoMenu() {
        Intent menu = new Intent(getApplicationContext(), RealMain.class);
        startActivity(menu);
        finish();
    }

    //permet de set la taille a chaque changement de listgenre
    private void setListViewHeight(ExpandableListView listView) {
        ExpandableListAdapter listAdapter = (ExpandableListAdapter) listView.getExpandableListAdapter();
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getGroupCount(); i++) {
            View groupView = listAdapter.getGroupView(i, true, null, listView);
            groupView.measure(0, View.MeasureSpec.UNSPECIFIED);
            totalHeight += groupView.getMeasuredHeight();

                for(int j = 0; j < listAdapter.getChildrenCount(i); j++){
                    View listItem = listAdapter.getChildView(i, j, false, null, listView);
                    listItem.measure(0, View.MeasureSpec.UNSPECIFIED);
                    totalHeight += listItem.getMeasuredHeight();
            }
        }

        ViewGroup.LayoutParams params = layutlist.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getGroupCount() - 1))+10;
        layutlist.setLayoutParams(params);
        layutlist.requestLayout();
    }

}

