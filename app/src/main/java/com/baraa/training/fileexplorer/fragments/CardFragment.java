package com.baraa.training.fileexplorer.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.baraa.training.fileexplorer.R;
import com.baraa.training.fileexplorer.adapters.FileAdapter;
import com.baraa.training.fileexplorer.databinding.FragmentCardBinding;
import com.baraa.training.fileexplorer.databinding.OptionDialogBinding;
import com.baraa.training.fileexplorer.databinding.OptionLayoutBinding;
import com.baraa.training.fileexplorer.others.FileOpener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CardFragment extends Fragment implements FileAdapter.OnFileSelectedListener {

    private FragmentCardBinding binding;
    private FileAdapter fileAdapter;
    private ArrayList<File> fileList;
    private File storage;
    private Dialog optionDialog;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint("SdCardPath")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCardBinding.inflate(inflater);

        TextView tvPathHolder = binding.tvPathHolder;

/*        File[] externalCacheDirs = requireContext().getExternalCacheDirs();
        for (File file : externalCacheDirs){
            if(Environment.isExternalStorageRemovable(file)){
                String secStorage = file.getPath().split("/Android")[0];
                break;
            }
        }
*/

        String data = getArguments() != null ? getArguments().getString("path") : null;
        if (data != null)
            storage = new File(data);
        else
            storage = Environment.getRootDirectory(); // Any thing

        tvPathHolder.setText(storage.getAbsolutePath());

        runTimePermission();

        return binding.getRoot();
    }

    private void runTimePermission() {
        Dexter.withContext(getContext())
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        displayFiles();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                    }
                }).check();
    }

    private ArrayList<File> findFiles(File file) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        if (files != null) {
            for (File singleFile : files)
                if (singleFile.isDirectory() && !singleFile.isHidden())
                    arrayList.add(singleFile);

            for (File singleFile : files) {
                String lowercaseName = singleFile.getName().toLowerCase();
                if (lowercaseName.endsWith(".jpeg") || lowercaseName.endsWith(".jpg") ||
                        lowercaseName.endsWith(".png") || lowercaseName.endsWith(".mp3") ||
                        lowercaseName.endsWith(".wav") || lowercaseName.endsWith(".mp4") ||
                        lowercaseName.endsWith(".pdf") || lowercaseName.endsWith(".doc") ||
                        lowercaseName.endsWith(".apk"))
                    arrayList.add(singleFile);
            }
        }
        return arrayList;
    }

    private void displayFiles() {
        fileList = new ArrayList<>();
        fileList.addAll(findFiles(storage));

        fileAdapter = new FileAdapter(requireContext(), fileList, this);
        binding.recyclerCard.setHasFixedSize(true);
        binding.recyclerCard.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerCard.setAdapter(fileAdapter);
    }

    @Override
    public void onFileClicked(File file) {
        if (file.isDirectory()) {
            Bundle bundle = new Bundle();
            bundle.putString("path", file.getAbsolutePath());

            CardFragment cardFragment = new CardFragment();
            cardFragment.setArguments(bundle);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.container, cardFragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            try {
                new FileOpener().openFile(getContext(), file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    public void onFileLongClicked(@NonNull File file, int position) {
        OptionDialogBinding bindingOptionDialog = OptionDialogBinding.inflate(getLayoutInflater());
        optionDialog = new Dialog(requireContext());
        optionDialog.setContentView(bindingOptionDialog.getRoot());
        optionDialog.setTitle("Select Options");

        bindingOptionDialog.listView.setAdapter(new CustomAdapter());

        bindingOptionDialog.listView.setOnItemClickListener((parent, view, position1, id) -> {
            optionDialog.cancel();

            switch ((String) parent.getItemAtPosition(position1)) {
                case "Details":
                    AlertDialog.Builder detailDialog = new AlertDialog.Builder(requireContext());
                    detailDialog.setTitle("Details");
                    TextView details = new TextView(requireContext());
                    detailDialog.setView(details);
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    String formattedDate = formatter.format(new Date(file.lastModified()));

                    details.setText("File name: " + file.getName() + "\n\n" +
                            "Size: " + Formatter.formatShortFileSize(requireContext(), file.length()) + "\n\n" +
                            "Path: " + file.getAbsolutePath() + "\n\n" +
                            "Last Modified: " + formattedDate);

                    detailDialog.setPositiveButton("Ok", (dialog, which) -> {
                    });

                    detailDialog.create();
                    detailDialog.show();
                    break;

                case "Rename":
                    AlertDialog.Builder renameDialog = new AlertDialog.Builder(requireContext());
                    renameDialog.setTitle("Rename File:");
                    EditText name = new EditText(requireContext());
                    renameDialog.setView(name);

                    renameDialog.setPositiveButton("Ok", (dialog, which) -> {
                        String newName = name.getText().toString();
                        File current = new File(file.getAbsolutePath());
                        File destination = new File(file.getAbsolutePath().replace(file.getName(), newName) + file.getName());

                        if (current.renameTo(destination)) {
                            fileList.set(position, destination);
                            fileAdapter.notifyDataSetChanged();
                            Toast.makeText(requireContext(), "Renamed!", Toast.LENGTH_LONG).show();
                        } else
                            Toast.makeText(requireContext(), "Couldn't rename!", Toast.LENGTH_LONG).show();
                    });

                    renameDialog.setNegativeButton("Cancel", (dialog, which) -> optionDialog.cancel());
                    renameDialog.create();
                    renameDialog.show();
                    break;

                case "Delete":
                    AlertDialog.Builder deleteDialog = new AlertDialog.Builder(requireContext());
                    deleteDialog.setTitle("Delete " + file.getName() + "?");
                    deleteDialog.setPositiveButton("Yes", (dialog, which) -> {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                        fileList.remove(file);
                        fileAdapter.notifyDataSetChanged();
                        Toast.makeText(requireContext(), "Deleted!", Toast.LENGTH_LONG).show();
                    });
                    deleteDialog.setNegativeButton("No", (dialog, which) -> optionDialog.cancel());
                    deleteDialog.create();
                    deleteDialog.show();
                    break;

                case "Share":
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("image/jpeg");
            }
        });
    }

    public static class CustomAdapter extends BaseAdapter {
        private final ArrayList<String> items = new ArrayList<>();

        public CustomAdapter() {
            items.addAll(Arrays.asList("Details", "Rename", "Delete", "Share"));
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            OptionLayoutBinding binding = OptionLayoutBinding.inflate(inflater);

            TextView tvOption = binding.tvOption;
            ImageView imgOption = binding.imgOption;
            tvOption.setText(items.get(position));

            switch (items.get(position)) {
                case "Details":
                    imgOption.setImageResource(R.drawable.ic_details);
                    break;
                case "Rename":
                    imgOption.setImageResource(R.drawable.ic_rename);
                    break;
                case "Delete":
                    imgOption.setImageResource(R.drawable.ic_delete);
                    break;
                case "Share":
                    imgOption.setImageResource(R.drawable.ic_share);
                    break;
            }
            return binding.getRoot();
        }
    }
}