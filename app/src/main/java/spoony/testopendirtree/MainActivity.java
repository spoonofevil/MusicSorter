package spoony.testopendirtree;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.net.URL;
import java.util.Optional;


public class MainActivity extends ListActivity {

    //XML components
    public Button cancel;
    public Button confirm;
    public TextView TittleDir;

    //var
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (shouldAskPermissions()) {
            askPermissions();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.TittleDir = findViewById(R.id.Tittledir);

        //retour au menu
        this.cancel = findViewById(R.id.abort);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent menu = new Intent(MainActivity.this, RealMain.class);
                startActivity(menu);
                finish();
            }
        });

        //choisi le folder dans lequel le user est
        this.confirm = findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent trimusic = new Intent(MainActivity.this, AffichName.class);

                //path de l'internal storage de mon tel (jsp comment avoir le path automatiquement) au cas ou le path ne s'est pas retrouvé correctement
                path = "/mnt/user/0/primary/";

                //recupere le path dans le bundle (infos donné d'acticité en activité)
                if (getIntent().hasExtra("path")) {
                    path = getIntent().getStringExtra("path");
                }
                File dir = new File(path);
                System.out.println("le path : "+path);


                ArrayList<String> musiclist = new ArrayList<>();
                List<String> MediaType = new ArrayList();
                MediaType.add(".mp3");
                MediaType.add(".wav");
                if (shouldAskPermissions()) {
                    askPermissions();
                }
                //pour empecher de choisir un dossier priver et aussi faire la difference entre des dossier vides et ceux ou on a pas le droit de lecteur
                if (!dir.canRead()) {
                    TittleDir.setText(getTitle() + " (inaccessible)");
                    Toast.makeText(MainActivity.this, "Private folder take another one plz", Toast.LENGTH_SHORT).show();
                }else {

                    //list tout le contenu du dossier dir
                    String[] list = dir.list();
                    if (list != null) {
                        for (String file : list) {
                            //list tout les music finissant en .mp3 et .m4a
                            if (file.endsWith(MediaType.get(0)) || file.endsWith(MediaType.get(1)))
                                musiclist.add(file);
                        }
                    }
                    Collections.sort(musiclist);//tri la list de musique

                    if(musiclist.isEmpty()){
                        Toast.makeText(MainActivity.this, "No music found please find another folder", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);

                        //si le parent n'est pas lisisble on reste dans le meme folder sinon on remonte
                        if(new File(dir.getParent()).canRead()) intent.putExtra("path", dir.getParent());
                        else intent.putExtra("path", dir);
                        
                        System.out.println(dir.getParent());
                        startActivity(intent);
                    }else {
                        //met dans le bundle le path et les music du Dir
                        Bundle b = new Bundle();
                        b.putString("Dir", path);
                        System.out.println(musiclist);
                        b.putStringArrayList("content", musiclist);
                        trimusic.putExtras(b);

                        //lance l'activité de tri
                        startActivity(trimusic);
                        finish();
                    }
                }
            }
        });

        //path de l'internal storage de mon tel (jsp comment avoir le path automatiquement)
        path = "/mnt/user/0/primary/";

        //si l'activité a deja etait appeler alors le path est celui donné dans le bundle (infos donné d'acticité en activité)
        if (getIntent().hasExtra("path")) {
            path = getIntent().getStringExtra("path");
        }

        // Use the current directory as title
        System.out.println(path);
        TittleDir.setText(path);

        List<String> folder = new ArrayList();
        List<String> values = new ArrayList();
        File dir = new File(path);

        //pour faire la difference entre des dossier vides et ceux ou on a pas le droit de lecture
        if (!dir.canRead()) {
            TittleDir.setText(path + " (inaccessible)");
        }


        //liste le contenu d'un folder et le met dans 2 liste séparant fichier et dossier
        String[] list = dir.list();
        if (list != null) {
            for (String file : list) {
                String filepath;
                if (path.endsWith(File.separator)) {
                    filepath = path + file;
                } else {
                    filepath = path + File.separator + file;
                }
                if (new File(filepath).isDirectory()) {
                    folder.add(file);
                } else {
                    values.add(file);
                }
            }
        }
        Collections.sort(values);
        Collections.sort(folder);

        //pour s'y retrouver je met 2 faux fichier delimittant les dossier et fichiers
        if(!values.isEmpty()) values.add(0,"---MUSIC FILE(S)---");
        if(!folder.isEmpty()) folder.add(0,"---FOLDER(S)---");
        folder.addAll(values);

        // rempli la listview avec la list faite precedemment
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_2, android.R.id.text1, folder);
        setListAdapter(adapter);
    }
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
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String filename = (String) getListAdapter().getItem(position);
        //evite les pb avec des fichiers qui n'existe pas (UPDATE A FAIRE : tester si des fichiers du meme noms sont veritablement (si il ya vrmt un dossier ---FOLDER(S)---)
        if(!filename.equals("---MUSIC FILE(S)---") && !filename.equals("---FOLDER(S)---")) {

            //rajoute le / a la fin ou pas
            String filenamepath;
            if (path.endsWith(File.separator)) {
                filenamepath = path + filename;
            } else {
                filenamepath = path + File.separator + filename;
            }

            //si c un folder alors ca relance cette activité en mettant en bundle le nouveau path : le folder choisi
            if (new File(filenamepath).isDirectory()) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("path", filenamepath);
                System.out.println(filenamepath);
                startActivity(intent);
            } else if(filename.endsWith(".mp3") || filename.endsWith(".m4a")){
                //lance l'activité de tri avec juste 1 musique
                Intent trimusic = new Intent(this, AffichName.class);

                Bundle b = new Bundle();
                ArrayList<String> file = new ArrayList<>();
                file.add(filename);

                b.putString("Dir", path);
                b.putStringArrayList("content", file);
                trimusic.putExtras(b);

                System.out.println(filename);

                startActivity(trimusic);
                finish();
            }else{
                Toast.makeText(this, filename + " is not a directory nor a music file", Toast.LENGTH_LONG).show();
            }
        }
    }

}