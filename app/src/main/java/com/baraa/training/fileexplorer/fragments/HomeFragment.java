package com.baraa.training.fileexplorer.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.baraa.training.fileexplorer.R;
import com.baraa.training.fileexplorer.adapters.FileAdapter;
import com.baraa.training.fileexplorer.databinding.FragmentHomeBinding;
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
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment implements FileAdapter.OnFileSelectedListener, View.OnClickListener {

    private FragmentHomeBinding binding;
    private FileAdapter fileAdapter;
    private ArrayList<File> fileList;
//    private File storage;
    private Dialog optionDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater);

        binding.imgImages.setOnClickListener(this);
        binding.tvImages.setOnClickListener(this);
        binding.imgVideos.setOnClickListener(this);
        binding.imgVideos.setOnClickListener(this);
        binding.imgMusics.setOnClickListener(this);
        binding.imgMusics.setOnClickListener(this);
        binding.imgDocs.setOnClickListener(this);
        binding.imgDocs.setOnClickListener(this);
        binding.imgDownloads.setOnClickListener(this);
        binding.imgDownloads.setOnClickListener(this);
        binding.imgApks.setOnClickListener(this);
        binding.imgApks.setOnClickListener(this);

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
            for (File singleFile : files) {
                if (singleFile.isDirectory() && !singleFile.isHidden())
                    arrayList.addAll(findFiles(singleFile));

                else if (singleFile.getName().toLowerCase().endsWith(".jpeg") || singleFile.getName().toLowerCase().endsWith(".jpg") ||
                        singleFile.getName().toLowerCase().endsWith(".png") || singleFile.getName().toLowerCase().endsWith(".mp3") ||
                        singleFile.getName().toLowerCase().endsWith(".wav") || singleFile.getName().toLowerCase().endsWith(".mp4") ||
                        singleFile.getName().toLowerCase().endsWith(".pdf") || singleFile.getName().toLowerCase().endsWith(".doc") ||
                        singleFile.getName().toLowerCase().endsWith(".apk")) {

                    arrayList.add(singleFile);
                }
            }
        }
        arrayList.sort(Comparator.comparing(File::lastModified).reversed());
        return arrayList;
    }

    private void displayFiles() {
        fileList = new ArrayList<>();
        fileList.addAll(findFiles(Environment.getExternalStorageDirectory()));

        fileAdapter = new FileAdapter(requireContext(), fileList, this);
        binding.recyclerRecents.setHasFixedSize(true);
        binding.recyclerRecents.setLayoutManager(new GridLayoutManager(getContext(), 3));
        binding.recyclerRecents.setAdapter(fileAdapter);
    }

    @Override
    public void onFileClicked(File file) {
        if (file.isDirectory()) {
            Bundle bundle = new Bundle();
            bundle.putString("path", file.getAbsolutePath());

            HomeFragment homeFragment = new HomeFragment();
            homeFragment.setArguments(bundle);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.container, homeFragment)
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

    @Override
    public void onClick(View v) {
        if (v.getId() == binding.imgImages.getId() || v.getId() == binding.tvImages.getId()){
            Bundle args = new Bundle();
            args.putString("fileType", "images");
            CategorizedFragment categorizedFragment = new CategorizedFragment();
            categorizedFragment.setArguments(args);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, categorizedFragment)
                    .addToBackStack(null).commit();

        } else if (v.getId() == binding.imgVideos.getId() || v.getId() == binding.tvVideos.getId()) {
            Bundle args = new Bundle();
            args.putString("fileType", "videos");
            CategorizedFragment categorizedFragment = new CategorizedFragment();
            categorizedFragment.setArguments(args);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, categorizedFragment)
                    .addToBackStack(null).commit();

        } else if (v.getId() == binding.imgMusics.getId() || v.getId() == binding.tvMusics.getId()) {
            Bundle args = new Bundle();
            args.putString("fileType", "musics");
            CategorizedFragment categorizedFragment = new CategorizedFragment();
            categorizedFragment.setArguments(args);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, categorizedFragment)
                    .addToBackStack(null).commit();

        } else if (v.getId() == binding.imgDocs.getId() || v.getId() == binding.tvDocs.getId()) {
            Bundle args = new Bundle();
            args.putString("fileType", "docs");
            CategorizedFragment categorizedFragment = new CategorizedFragment();
            categorizedFragment.setArguments(args);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, categorizedFragment)
                    .addToBackStack(null).commit();

        } else if (v.getId() == binding.imgDownloads.getId() || v.getId() == binding.tvDownloads.getId()) {
            Bundle args = new Bundle();
            args.putString("fileType", "downloads");
            CategorizedFragment categorizedFragment = new CategorizedFragment();
            categorizedFragment.setArguments(args);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, categorizedFragment)
                    .addToBackStack(null).commit();

        } else if (v.getId() == binding.imgApks.getId() || v.getId() == binding.tvApks.getId()) {
            Bundle args = new Bundle();
            args.putString("fileType", "apks");
            CategorizedFragment categorizedFragment = new CategorizedFragment();
            categorizedFragment.setArguments(args);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, categorizedFragment)
                    .addToBackStack(null).commit();
        }
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
