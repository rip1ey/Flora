package com.asg.florafauna;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import static android.widget.Toast.LENGTH_LONG;

public class PersonalRecordingsActivity extends AppCompatActivity {

    private Bitmap currentImage;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_WRITE_EXTERNAL_STORAGE = 0;

    private Intent PR_Directory;
    private String dirName;// = Environment.getExternalStorageDirectory().toString() + "/FloraFauna/Recordings/";
    private File recordings;// = new File(dirName);
    private String[] themeArray = new String[1];

    // List Files
    GridView imagegrid;
    ArrayList<String> FilePathStrings = new ArrayList<String>();// list of file paths
    File[] listFile;
    ArrayList<String> FileNameStrings = new ArrayList<String>();
    AlertDialog imageDialog;
    AlertDialog folderDialog;
    String folderName;
    boolean nameGiven = true;
    ArrayAdapter<String> spinAdapter;
    ArrayList<File> folderAL = new ArrayList<File>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setTheme(R.style.AppTheme);
        try {
            //opens the file to read its contents
            FileInputStream fis = this.openFileInput("theme");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            themeArray[0] = reader.readLine(); //adds the line to the temp array
            reader.close();
            isr.close();
            fis.close();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        if (themeArray[0].equals("Green")){
            setTheme(R.style.AppTheme);
        }
        else if (themeArray[0].equals("Blue")){
            setTheme(R.style.AppThemeBlue);
        }
        else if (themeArray[0].equals("Mono")){
            setTheme(R.style.AppThemeMono);
        }
        else if (themeArray[0].equals("Cherry")){
            setTheme(R.style.AppThemeCherry);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_recordings);

        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_recordings);

        //traverse directories-------------------------------------------------------
        PR_Directory = getIntent();
        //-------------------------------------
        //Toast.makeText(this, PR_Directory.getStringExtra("RDIR"), Toast.LENGTH_LONG).show();
        if (PR_Directory.getStringExtra("RDIR") == null) {
            dirName = Environment.getExternalStorageDirectory().toString() + "/FloraFauna/Recordings/";
            this.setTitle("YESSS");

        }
        else{
            dirName = PR_Directory.getStringExtra("RDIR");

            //split the directory path into an array
            String[] name = dirName.split("/");

            //call the textView on the action bar .xml file
            TextView textView = (TextView)findViewById(R.id.recordings_ab_text);

            //set the actionbar title text to current directory
            textView.setText(name[name.length - 1].substring(0,1).toUpperCase()
                    + name[name.length - 1].substring(1).toLowerCase());
        }

        //--------------------------------------------------------------------------------------
        //gallery button
        FloatingActionButton openGallery = (FloatingActionButton) findViewById(R.id.floatingUpload);
        openGallery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 1);
            }
        });

        // open the default camera app to take a picture
        ImageButton openCamera = (ImageButton) findViewById(R.id.floatingCameraButton);
        openCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, 1);
                }
            }
        });


        //Create dir for recordings
        //request for permission to write to storage
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_ACCESS_WRITE_EXTERNAL_STORAGE);
        }

        //set the directory to be read
        recordings = new File(dirName);

        //checks if the recordings dir exists
        if (!recordings.exists()) {
            //if directory creation fails, tell the user
            if (!recordings.mkdirs()) {
                Log.d("error", "failed to make dir");
                Toast.makeText(this, "Failed to create directory", LENGTH_LONG).show();
            }
        }
        //if the directory exists, make a log
        else {
            Log.d("error", "dir. already exists");
        }

        imagegrid = (GridView) findViewById(R.id.FileList);

        //Check if write storage has permission
        PackageManager pm = this.getPackageManager();
        int hasPerm = pm.checkPermission(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                this.getPackageName());
        if (hasPerm == PackageManager.PERMISSION_GRANTED) {
            //list files
            GetFiles();
            imagegrid.setAdapter(new ImageAdapter());
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.recordings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_home:
                Intent search_intent = new Intent(PersonalRecordingsActivity.this, SearchActivity.class);
                PR_Directory.removeExtra("RDIR");
                startActivity(search_intent);
                return true;
            case R.id.action_settings:
                Intent settings_intent = new Intent(PersonalRecordingsActivity.this, SettingsActivity.class);
                PR_Directory.removeExtra("RDIR");
                startActivity(settings_intent);
                return true;
            case R.id.action_help:
                Intent help_intent = new Intent(PersonalRecordingsActivity.this, HelpActivity.class);
                PR_Directory.removeExtra("RDIR");
                startActivity(help_intent);
                return true;
            case R.id.action_map:
                Intent intent = new Intent(PersonalRecordingsActivity.this, MapActivity.class);
                PR_Directory.removeExtra("RDIR");
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Log.i("result okay",  "okay");

            createImageDialog(data);


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                }
                else {
                    // Permission denied, Unable to create directory
                }
            }
        }
    }

    public void goBack(View view){
        /* closes the activity */
        PR_Directory.removeExtra("RDIR");
        setResult(RESULT_OK, null);
        finish();
    }

    //List Files
    public void GetFiles()
    {
        if (recordings.isDirectory())
        {
            listFile = recordings.listFiles();


            for (int i = 0; i < listFile.length; i++)
            {
                // Get the path of the image file
                FilePathStrings.add(listFile[i].getAbsolutePath());
                // Get the name image file
                FileNameStrings.add(listFile[i].getName());

            }
        }
    }

    public class ImageAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public ImageAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return FilePathStrings.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            File testfile = new File(FilePathStrings.get(position));
            if (convertView == null) {
                holder = new ViewHolder(); //create new ViewHolder (custom class)

                //inflates the galleryitem.xml layout to be used and populate the gridview
                convertView = mInflater.inflate(R.layout.galleryitem, null);
                holder.imageview = (ImageView) convertView.findViewById(R.id.thumbImage); //thumbnail
                holder.fileName = (TextView) convertView.findViewById(R.id.fileName); //name of text
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.itemCheckBox); //checkbox of item
                holder.delete = (TextView) convertView.findViewById(R.id.delete); //delete button
                holder.imgDescription = (TextView) convertView.findViewById(R.id.description); // image description

                convertView.setTag(holder);

                //set onCheckListerner for each item
                holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        if(isChecked){ //if item is checked

                        }
                        else //if item is unchecked from being checked
                        {

                        }

                    }
                });

                //set onclicklistener for delete for each item
                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //code to delete file here
                        //on click, confirm with pop-up,
                        //if true, delete
                        ConfirmDelete(position);
                    }
                });

                //set onclicklistener for each item
                holder.imageview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //if image, do this
                        File testFileClicked = new File(FilePathStrings.get(position));
                        if(!testFileClicked.isDirectory()) {
                            Intent i = new Intent(getApplicationContext(), FullScreenImage.class);
                            // Pass String arrays FilePathStrings
                            i.putExtra("filepath", FilePathStrings);
                            // Pass String arrays FileNameStrings
                            i.putExtra("filename", FileNameStrings);
                            // Pass click position
                            i.putExtra("position", position);
                            startActivity(i);
                        }

                        //if folder, set new directory and open new instance
                        else if(testFileClicked.isDirectory()){
                            //do stuff
                            PR_Directory = new Intent(getApplicationContext(), PersonalRecordingsActivity.class);
                            PR_Directory.putExtra("RDIR", FilePathStrings.get(position));
                            startActivity(PR_Directory);
                        }

                    }
                });
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            //set thumbnail
            final Bitmap myBitmap = BitmapFactory.decodeFile(FilePathStrings.get(position));
            if(!testfile.isDirectory()) {
                holder.imageview.setImageBitmap(myBitmap);
            }
            else if(testfile.isDirectory()){
                holder.imageview.setImageResource(R.drawable.folder);
            }
            //breakdown file path to get only file name
            String filepath = FilePathStrings.get(position);
            ArrayList<String> list = new ArrayList<String>(Arrays.asList(filepath.split("/")));

            // set text name
            // both file name and description come as one string, split by '!'
            // name is first, description is second
            String[] nameDescr = list.get(list.size() - 1).split("!");
            if(nameDescr.length > 0) {
                holder.fileName.setText(nameDescr[0]);
            }


            //set description
            //if not folder
            if(!testfile.isDirectory()) {
                if(nameDescr.length > 1)
                {
                    holder.imgDescription.setText(nameDescr[1]);
                }
            }
            //if folder
            else if(testfile.isDirectory()){
                    holder.imgDescription.setText("");
            }

            return convertView;
        }
    }
    class ViewHolder {
        ImageView imageview;
        TextView fileName;
        CheckBox checkBox;
        TextView delete;
        TextView imgDescription;
    }

    // function to create the custom alert dialog
    protected void createImageDialog(final Intent data)
    {
        // create a builder to add custom settings to
        // an alert dialog
        AlertDialog.Builder builder;

        // create alert dialog in personal recordings context
        builder = new AlertDialog.Builder(this);

        // create a view associated with the alert dialog xml file
        View dView = getLayoutInflater().inflate(R.layout.dialog_addimage, null);

        // connect all of the components
        final EditText description = (EditText) dView.findViewById(R.id.description);
        final EditText imageName = (EditText) dView.findViewById(R.id.nameImage);

        // this should result in an image being placed in a directory or on the page
        Button okayButton = (Button) dView.findViewById(R.id.setImageData);
        okayButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                //if name is empty, tell user
                if(imageName.getText().toString().equalsIgnoreCase("")){
                    Toast.makeText(getBaseContext(), "Please Input a File Name..", LENGTH_LONG).show();
                    nameGiven = false;
                    imageDialog.dismiss();
                    createImageDialog(data);
                }
                else {
                    nameGiven = true;
                }


                //test if previously name was given
                if(nameGiven) {
                    // if we have an image stored, let's make sure
                    // that an image name and description was entered
                    // additionally, we need to ensure a save location was selected
                    if (currentImage == null) {
                        Log.i("just a log", "log");

                        Uri photoUri = data.getData();
                        if (photoUri != null) {
                            //code to mess with images will be here
                            ContentResolver cr = getContentResolver();
                            try {
                                currentImage = MediaStore.Images.Media.getBitmap(cr, photoUri);
                                //selectedImage.setImageBitmap(currentImage); //set the image view to the current image
                                FileOutputStream output = new FileOutputStream(recordings + "/" + imageName.getText() + "!" + description.getText());
                                currentImage.compress(Bitmap.CompressFormat.PNG, 75, output); //save file
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            refresh();

                        }
                    }


                    imageDialog.dismiss();
                }
            }
        });

        // this should result in nothing added to either the page
        // or any of the directories
        Button cancelButton = (Button) dView.findViewById(R.id.cancelImage);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                imageDialog.dismiss();
            }
        });

        // using an array list to create entries for the save location spinner
        ArrayList<String> defaultDirs = new ArrayList<>();

        // if there are existing folders, populate those in the imageDialog spinner
        if(!folderAL.isEmpty())
        {
            for(int i = 0; i < folderAL.size(); i++)
            {
                defaultDirs.add(folderAL.get(i).getName());
            }
        }


        // add the two default settings
        defaultDirs.add("On Page");
        defaultDirs.add("Create New");

        spinAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, defaultDirs);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // just a TextView to display words, "Save Location"
        TextView saveLoc = (TextView) dView.findViewById(R.id.saveLocation);

        // the spinner's entries should be all existing directories in the F&F folder
        // the user should also have the ability to create a new folder
        // lastly, the user should be able to save a picture in the 'root' part of the page
        // "On Page" for now
        final Spinner dirSelector = (Spinner) dView.findViewById(R.id.dirSpinner);
        dirSelector.setAdapter(spinAdapter);

        dirSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // if "Create New" is selected, then open a prompt for the user to type in
                // the name of the new folder
                // if "On Page" is selected, place image on the personal recording page
                // else, place image in selected folder
                String selectedDir = dirSelector.getSelectedItem().toString();
                if(selectedDir.equals("Create New"))
                {
                    createFolderDialog();

                }
                else if(selectedDir.equals("On Page"))
                {
                    Log.i("Save on page", "image");
                }
                else
                {

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        builder.setView(dView);

        imageDialog = builder.create();

        if(!nameGiven){
            imageName.setTextColor(Color.RED);
            imageName.setHintTextColor(Color.RED);
        }

        imageDialog.setTitle("Save Image");
        imageDialog.show();
    }

    protected void createFolderDialog()
    {
        Log.i("selected new", "folder");
        // create a builder to add custom settings to
        // an alert dialog
        AlertDialog.Builder dirBuilder;


        // create alert dialog in personal recordings context
        dirBuilder = new AlertDialog.Builder(PersonalRecordingsActivity.this);

        // create a view associated with the alert dialog xml file
        View dirView = getLayoutInflater().inflate(R.layout.dialog_createdir, null);

        // three components of this dialog
        // dirText, cancelButton, saveButton
        final EditText newFolder = (EditText) dirView.findViewById(R.id.dirText);

        // don't create folder, just close dialog
        Button noSave = (Button) dirView.findViewById(R.id.cancelButton);
        noSave.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                folderDialog.dismiss();
            }
        });

        Button saveFolder = (Button) dirView.findViewById(R.id.saveButton);
        saveFolder.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // get the folder name and create the folder
                if(!newFolder.getText().toString().equalsIgnoreCase("")) {
                    folderName = newFolder.getText().toString();
                    // if folder doesn't already exist, create it
                    if(!folderAL.contains(folderName))
                    {
                        createNewFolder(folderName);

                    }
                }
                // alert that a name hasn't been entered
                else
                {
                    Toast.makeText(getBaseContext(), "Enter a folder name", LENGTH_LONG).show();
                }


            }
        });

        dirBuilder.setView(dirView);

        folderDialog = dirBuilder.create();
        folderDialog.setTitle("Create New Folder");
        folderDialog.show();



    }

    // function to create the custom alert dialog
    public void ConfirmDelete(final int position)
    {
        // create a builder to add custom settings to
        // an alert dialog
        AlertDialog.Builder builder;

        // create alert dialog in personal recordings context
        builder = new AlertDialog.Builder(this);

        // create a view associated with the alert dialog xml file
        View dView = getLayoutInflater().inflate(R.layout.dialog_confirmdelete, null);


        // this should result in an image being placed in a directory or on the page
        Button okayButton = (Button) dView.findViewById(R.id.yesButton);
        okayButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                File deleteThis = new File(FilePathStrings.get(position));
                //if directory, recursively delete
                if(deleteThis.isDirectory()){
                    String[] children = deleteThis.list();
                    for (int i = 0; i < children.length; i++)
                    {
                        new File(deleteThis, children[i]).delete();
                    }
                    deleteThis.delete();

                }
                else {
                    deleteThis.delete();
                }
                //refresh the activity
                refresh();
                imageDialog.dismiss();
            }
        });

        // this should result in nothing added to either the page
        // or any of the directories
        Button cancelButton = (Button) dView.findViewById(R.id.noButton);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                imageDialog.dismiss();
            }
        });

        builder.setView(dView);

        imageDialog = builder.create();

        imageDialog.setTitle("Confirm Delete?");
        imageDialog.show();
    }

    protected void refresh(){
        Intent refresh = new Intent(PersonalRecordingsActivity.this, PersonalRecordingsActivity.class);
        refresh.putExtra("RDIR", dirName);
        finish();
        startActivity(refresh);

    }

    // create a folder in the root path /FloraFauna/Recordings
    protected void createNewFolder(String pathname)
    {
        String folderPath = dirName + pathname;
        Log.i("root directory", folderPath);
        File newFolder = new File(folderPath);

        // create new folder and store it in the folder array list
        if(newFolder.mkdir())
        {
            folderAL.add(newFolder);
        }
        else
        {
            Toast.makeText(getBaseContext(), "Couldn't create folder", LENGTH_LONG).show();
        }

    }

}
