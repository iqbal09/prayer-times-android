package com.metinkale.prayerapp.utils;

import android.app.Activity;
import android.app.Dialog;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.Arrays;

public class FileChooser {
    private static final String PARENT_DIR = "..";

    @NonNull
    private final Activity activity;
    private ListView list;
    private Dialog dialog;
    private File currentPath;

    // filter on file extension
    @Nullable
    private String extension;

    public void setExtension(@Nullable String extension) {
        this.extension = extension == null ? null :
                extension.toLowerCase();
    }

    // file selection event handling
    public interface FileSelectedListener {
        void fileSelected(File file);
    }

    @NonNull
    public FileChooser setFileListener(FileSelectedListener fileListener) {
        this.fileListener = fileListener;
        return this;
    }

    private FileSelectedListener fileListener;

    public FileChooser(@NonNull Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity);
        list = new ListView(activity);
        list.setOnItemClickListener((parent, view, which, id) -> {
            String fileChosen = (String) list.getItemAtPosition(which);
            File chosenFile = getChosenFile(fileChosen);
            if (chosenFile.isDirectory()) {
                refresh(chosenFile);
            } else {
                if (fileListener != null) {
                    fileListener.fileSelected(chosenFile);
                }
                dialog.dismiss();
            }
        });
        dialog.setContentView(list);
        dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        refresh(Environment.getExternalStorageDirectory());
    }

    public void showDialog() {
        dialog.show();
    }


    /**
     * Sort, filter and display the files for the given path.
     */
    private void refresh(@NonNull File path) {
        currentPath = path;
        if (path.exists()) {
            File[] dirs = path.listFiles(file -> file.isDirectory() && file.canRead());
            File[] files = path.listFiles(file -> {
                if (file.isDirectory()) {
                    return false;
                } else {
                    if (!file.canRead()) {
                        return false;
                    } else if (extension == null) {
                        return true;
                    } else {
                        return file.getName().toLowerCase().endsWith(extension);
                    }
                }
            });

            if(files==null)files=new File[0];
            if(dirs==null)dirs  =new File[0];

            // convert to an array
            int i = 0;
            String[] fileList;
            if (path.getParentFile() == null) {
                fileList = new String[dirs.length + files.length];
            } else {
                fileList = new String[dirs.length + files.length + 1];
                fileList[i++] = PARENT_DIR;
            }
            Arrays.sort(dirs);
            Arrays.sort(files);
            for (File dir : dirs) {
                fileList[i++] = dir.getName();
            }
            for (File file : files) {
                fileList[i++] = file.getName();
            }

            // refresh the user interface
            dialog.setTitle(currentPath.getPath());
            list.setAdapter(new ArrayAdapter(activity,
                    android.R.layout.simple_list_item_1, fileList) {
                @NonNull
                @Override
                public View getView(int pos, View view, @NonNull ViewGroup parent) {
                    view = super.getView(pos, view, parent);
                    ((TextView) view).setSingleLine(true);
                    return view;
                }
            });
        }
    }


    /**
     * Convert a relative filename into an actual File object.
     */
    private File getChosenFile(@NonNull String fileChosen) {
        if (fileChosen.equals(PARENT_DIR)) {
            return currentPath.getParentFile();
        } else {
            return new File(currentPath, fileChosen);
        }
    }
}