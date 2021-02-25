package spoony.testopendirtree;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class RealMain extends AppCompatActivity {

    private Context Filedialog; //si un jour le context est utilisé (pour l'instant nn...)

    //XML components
    private Button chooseSD;
    private Button chooseInter;
    private RealMain activity;
    private Button openlistgenr;
    private Button cleargenre;
    private TextView listgenretest;

    private static final int requestcode = 123; //pour la demande de perm c le code demandé askip

    //variables
    int indicegenretoadd;
    List<String> musigGenreList = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startmenu);

        //met les Id du XML a chaque objet java
        this.chooseSD = findViewById(R.id.choosSD);
        this.listgenretest=findViewById(R.id.listgenretext);
        this.activity = this;

        //lance le choisisseur de dossier dans la SD card avec la demande de perms
        chooseSD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifypermSD();
            }
        });

        //lance le choisisseur de dossier dans le /Storage card avec la demande de perms
        this.chooseInter = findViewById(R.id.choosIntern);
        chooseInter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifypermInter();
            }
        });

        //montre les genres sauvegardés
        this.openlistgenr=findViewById(R.id.opengenrelist);
        openlistgenr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //ouvre la save et recup la list des genres
                SharedPreferences SP = getSharedPreferences("PREF", 0);
                String listinstring = SP.getString("Genres","");

                //si la save est vide alors ca ouvre un popup pour "remplir" cette liste
                if(TextUtils.isEmpty(listinstring) || listinstring.equals("[]") || listinstring.equals(",")) {

                    //signal le user que la save est vide
                    Toast.makeText(RealMain.this, "no genre saved please enter at least one", Toast.LENGTH_SHORT).show();
                    System.out.println("la save est vide");
                    listgenretest.setText("empty save");

                    //puis crée le popup pour en ajouter
                    PopupAddGenre Popubaddgenre = new PopupAddGenre(activity);
                    musigGenreList = new ArrayList<>();
                    Popubaddgenre.getConfirmaddedgenre().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //save la list sous formes de string
                            Toast.makeText(RealMain.this, "Changing saved genre list...", Toast.LENGTH_SHORT).show();
                            SharedPreferences SP = getSharedPreferences("PREF", 0);
                            SharedPreferences.Editor editor = SP.edit();

                            //pour transformer la liste en une string
                            StringBuilder SB = new StringBuilder();
                            System.out.println("la liste a update " + musigGenreList);
                            for (String g : musigGenreList) {
                                SB.append(g + ",");
                            }
                            SB.deleteCharAt(SB.length() - 1); //pour enlever la virgule du dernier genre add

                            System.out.println("new listgenre " + SB);
                            editor.putString("Genres", SB.toString());
                            editor.apply();
                            listgenretest.setText(SB);//montre la nouvelle liste a l'user
                            Popubaddgenre.dismiss();
                        }
                    });
                    Popubaddgenre.getaddgenre().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            //si le user a rien mit ca fait rien
                            if (Popubaddgenre.getInputGenre().getText() == "" || TextUtils.isEmpty(Popubaddgenre.getInputGenre().getText()) || Popubaddgenre.getInputGenre().getText() == " ") {
                                Toast.makeText(RealMain.this, "please enter a genre", Toast.LENGTH_SHORT).show();
                            } else {
                                if (TextUtils.isEmpty(Popubaddgenre.getGenreadded().getText())) {// si il ya pas d'autre genre ca met pas la virgule avant
                                    Popubaddgenre.getGenreadded().setText(Popubaddgenre.getInputGenre().getText());
                                } else {
                                    Popubaddgenre.getGenreadded().setText(Popubaddgenre.getGenreadded().getText() + "," + Popubaddgenre.getInputGenre().getText());
                                }
                                System.out.println("c la normalement :" + Popubaddgenre.getGenreadded().getText() + ".");

                                musigGenreList.add(Popubaddgenre.getInputGenre().getText() + "");//ajoute le genre mis par le user dans la liste
                                TextView genretoadTextview = new TextView(RealMain.this);//rajoute le textview pour l'enlever si le user change d'avis
                                //ViewGroup.LayoutParams params = new ActionBar.LayoutParams();
                                genretoadTextview.setText(Popubaddgenre.getInputGenre().getText());

                                //pour l'enlever de la liste il la met sous forme de textviews pour pouvoir enlever un ou plusieurs elements lorsqu'il sont touchés
                                genretoadTextview.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        indicegenretoadd = 0;
                                        System.out.println("on enleve " + genretoadTextview.getText());

                                        //passer par un for evite d'enlevé toute les occurences d'un meme genre (et se retrouver avec une textview qui n'a pas de correspondance avec la liste print en bas et celle sauvergardé)
                                        for (String identik : musigGenreList) {
                                            //System.out.println("num "+indicegenretoadd+ "/"+identik+"/");
                                            if (identik.equals(genretoadTextview.getText().toString())) {
                                                //System.out.println(indicegenretoadd+"="+musigGenreList.get(indicegenretoadd));
                                                musigGenreList.remove(indicegenretoadd);
                                                break;
                                            }
                                            indicegenretoadd++;
                                        }
                                        //musigGenreList.remove(genretoadTextview.getText());

                                        //permet de mettra a jour en buildant une string qui sera affichée et stockée (a la fin) dans la save (et affiché dans la expandable view bien sur)
                                        StringBuilder SB2 = new StringBuilder();
                                        for (String genress : musigGenreList) {
                                            SB2.append(genress + ",");
                                        }

                                        //enleve si il ya un ou plusieurs virgule a la fin (il y en a tjr au moins une)
                                        while (SB2.toString().endsWith(",")) {
                                            SB2.deleteCharAt(SB2.length() - 1);
                                        }
                                        System.out.println("la liste " + SB2.toString());

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
                }else{
                    listgenretest.setText(listinstring); //print la liste si il y en a une
                }

            }
        });

        //reset la save (avec un popub de confirmation bien sur)
        this.cleargenre = findViewById(R.id.clearSavedgenre);
        cleargenre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder InfoPopupConfirm = new AlertDialog.Builder(activity);
                InfoPopupConfirm.setTitle("Confirm RESET");
                InfoPopupConfirm.setMessage("Are you sure you want to reset the genres lists ?");
                InfoPopupConfirm.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(RealMain.this, "Resetting genres...", Toast.LENGTH_SHORT).show();
                        InfoPopupConfirm.show();
                        SharedPreferences SP = getSharedPreferences("PREF",0);
                        SharedPreferences.Editor editor = SP.edit();
                        editor.clear();
                        editor.apply();
                        listgenretest.setText("empty save");
                    }
                });
                InfoPopupConfirm.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(RealMain.this, "Operation cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    //verifie les perms (askip faut le faire avant chaque action)
    private void verifypermInter(){
        //Log.d(TAG, "verifyperm: asking user perm");
        String [] permissions= {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),permissions[1]) == PackageManager.PERMISSION_GRANTED){
            Intent FileChooser = new Intent(getApplicationContext(), MainActivity.class);
            FileChooser.putExtra("path", "/mnt/user/0/primary/");
            startActivity(FileChooser);
            finish();
            //System.out.println("oui");
        }else{
            ActivityCompat.requestPermissions(RealMain.this,permissions,requestcode);
            //System.out.println("non");
        }

    }
    private void verifypermSD(){
        //Log.d(TAG, "verifyperm: asking user perm");
        String [] permissions= {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),permissions[1]) == PackageManager.PERMISSION_GRANTED){
            Intent FileChooser = new Intent(getApplicationContext(), MainActivity.class);
            FileChooser.putExtra("path", "/storage/0A77-D026/");
            startActivity(FileChooser);
            finish();
            //System.out.println("oui");
        }else{
            ActivityCompat.requestPermissions(RealMain.this,permissions,requestcode);
            //System.out.println("non");
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        verifypermSD();
    }
}