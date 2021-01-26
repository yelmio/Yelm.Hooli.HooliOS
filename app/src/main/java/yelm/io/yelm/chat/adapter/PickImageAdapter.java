package yelm.io.yelm.chat.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import yelm.io.yelm.databinding.CameraPreviewLayoutBinding;
import yelm.io.yelm.databinding.PickStorageImageBinding;
import yelm.io.yelm.support_stuff.AlexTAG;
import yelm.io.yelm.support_stuff.ScreenDimensions;

public class PickImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final String[] CAMERA_PERMISSIONS = new String[]{Manifest.permission.CAMERA};

    private ArrayList<String> listImages;
    private Context context;
    ScreenDimensions screenDimensions;
    private Listener listener;
    private CameraListener cameraListener;
    boolean[] checked;

    public interface CameraListener {
        void onClick();
    }

    public interface Listener {
        void selectedPicture(Integer position, String path, boolean check);
    }
    public void setCameraListener(CameraListener cameraListener) {
        this.cameraListener = cameraListener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public PickImageAdapter(Context context, ArrayList<String> listImages) {
        this.listImages = listImages;
        this.context = context;
        this.screenDimensions = new ScreenDimensions((Activity) context);
        checked = new boolean[listImages.size()];
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return new PickImageAdapter.HeaderViewHolder(CameraPreviewLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else {
            return new PickImageAdapter.ItemViewHolder(PickStorageImageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
    }

    private boolean hasCameraPermission() {
        int result = ContextCompat
                .checkSelfPermission(context, CAMERA_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }



    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof HeaderViewHolder) {
            CameraX.unbindAll();
            if (hasCameraPermission()) {
                Runnable startCamera = new Runnable() {
                    @Override
                    public void run() {
                        // Create configuration object for the viewfinder use case
                        PreviewConfig previewConfig = new PreviewConfig.Builder()
                                .setTargetAspectRatio(new Rational(1, 1))
                                .setTargetResolution(new Size(640, 640))
                                .build();

                        // Build the viewfinder use case
                        Preview preview = new Preview(previewConfig);

                        // Every time the viewfinder is updated, recompute layout
                        preview.setOnPreviewOutputUpdateListener(
                                previewOutput -> {
                                    // To update the SurfaceTexture, we have to remove it and re-add it
                                    ViewGroup parent = (ViewGroup) ((HeaderViewHolder) holder).binding.viewFinder.getParent();
                                    parent.removeView(((HeaderViewHolder) holder).binding.viewFinder);
                                    parent.addView(((HeaderViewHolder) holder).binding.viewFinder, 0);

                                    ((HeaderViewHolder) holder).binding.viewFinder.setSurfaceTexture(previewOutput.getSurfaceTexture());
                                    updateTransform(holder);
                                });

                        // Create configuration object for the image capture use case
                        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder()
                                .setTargetAspectRatio(new Rational(1, 1))
                                // We don't set a resolution for image capture; instead, we
                                // select a capture mode which will infer the appropriate
                                // resolution based on aspect ration and requested mode
                                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                                .build();

                        // Build the image capture use case and attach button click listener
                        ImageCapture imageCapture = new ImageCapture(imageCaptureConfig);
//            binding.capture.setOnClickListener(view -> {
//                File file = new File(getExternalMediaDirs()[0], System.currentTimeMillis() + ".jpg");
//
//                imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener(){
//
//                    @Override
//                    public void onError(ImageCapture.UseCaseError error, String message,
//                                        @Nullable Throwable exc) {
//                        String msg = "Photo capture failed: " + message;
//                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
//                        Log.e(TAG, msg);
//                        if (exc != null) {
//                            exc.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onImageSaved(File file) {
//                        String msg = "Photo capture succeeded: " + file.getAbsolutePath();
//                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
//                        Log.d(TAG, msg);
//                    }
//                });
//
//            });

                        // Setup image analysis pipeline that computes average pixel luminance
                        HandlerThread analyzerThread = new HandlerThread("LuminosityAnalysis");
                        analyzerThread.start();
                        ImageAnalysisConfig analyzerConfig =
                                new ImageAnalysisConfig.Builder()
                                        .setCallbackHandler(new Handler(analyzerThread.getLooper()))
                                        // In our analysis, we care more about the latest image than
                                        // analyzing *every* image
                                        .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                                        .build();

                        ImageAnalysis analyzerUseCase = new ImageAnalysis(analyzerConfig);
                        analyzerUseCase.setAnalyzer(new LuminosityAnalyzer());

                        // Bind use cases to lifecycle
                        CameraX.bindToLifecycle((LifecycleOwner) context, preview, imageCapture,
                                analyzerUseCase);
                    }
                };

                ((HeaderViewHolder) holder).binding.viewFinder.setOnClickListener(v -> {
                    if (cameraListener != null) {
                        cameraListener.onClick();
                    }

                });

                ((HeaderViewHolder) holder).binding.viewFinder.post(startCamera);

                ((HeaderViewHolder) holder).binding.viewFinder.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(
                            View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                            int oldRight, int oldBottom) {
                        updateTransform(holder);
                    }
                });
            }


        } else {
            String path = listImages.get(position - 1);
            ((ItemViewHolder) holder).binding.selector.setChecked(checked[position - 1]);
            //holder.binding.image.getLayoutParams().height = (int) (((screenDimensions.getWidthDP() - 48) / 3) * screenDimensions.getScreenDensity() + 0.5f);
            Picasso.get().load(Uri.fromFile(new File(path)))
                    .resize(200, 0)
                    .into(((ItemViewHolder) holder).binding.image);

            ((ItemViewHolder) holder).binding.selector.setOnClickListener(view -> {
                checked[position - 1] = !checked[position - 1];
                if (listener != null) {
                    listener.selectedPicture(position - 1, path, checked[position - 1]);
                }
            });

        }
    }

    private class LuminosityAnalyzer implements ImageAnalysis.Analyzer {
        private long lastAnalyzedTimestamp = 0L;

        @Override
        public void analyze(ImageProxy image, int rotationDegrees) {
            long currentTimestamp = System.currentTimeMillis();
            if (currentTimestamp - lastAnalyzedTimestamp >=
                    TimeUnit.SECONDS.toMillis(1)) {
                // Since format in ImageAnalysis is YUV, image.planes[0]
                // contains the Y (luminance) plane
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                // Extract image data from callback object
                byte[] data = toByteArray(buffer);

                // Convert the data into an array of pixel values
                int sum = 0;
                for (byte val : data) {
                    // Add pixel value
                    sum += (((int) val) & 0xFF);
                }
                // Compute average luminance for the image
                double luma = sum / ((double) data.length);
                Log.d(AlexTAG.debug, "Average Luminosity " + luma);
                // Update timestamp of last analyzed frame
                lastAnalyzedTimestamp = currentTimestamp;
            }
        }
    }

    private static byte[] toByteArray(ByteBuffer buffer) {
        buffer.rewind();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        return data;
    }

    private void updateTransform(RecyclerView.ViewHolder holder) {
        Matrix matrix = new Matrix();

        float centerX = ((PickImageAdapter.HeaderViewHolder) holder).binding.viewFinder.getWidth() / 2f;
        float centerY = ((PickImageAdapter.HeaderViewHolder) holder).binding.viewFinder.getHeight() / 2f;

        // Correct preview output to account for display rotation
        float rotationDegrees;
        switch (((PickImageAdapter.HeaderViewHolder) holder).binding.viewFinder.getDisplay().getRotation()) {
            case Surface.ROTATION_0:
                rotationDegrees = 0f;
                break;
            case Surface.ROTATION_90:
                rotationDegrees = 90f;
                break;
            case Surface.ROTATION_180:
                rotationDegrees = 180f;
                break;
            case Surface.ROTATION_270:
                rotationDegrees = 270f;
                break;
            default:
                return;
        }

        matrix.postRotate(-rotationDegrees, centerX, centerY);

        // Finally, apply transformations to our TextureView
        ((PickImageAdapter.HeaderViewHolder) holder).binding.viewFinder.setTransform(matrix);
    }

    @Override
    public int getItemCount() {
        return listImages.size() + 1;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        PickStorageImageBinding binding;

        public ItemViewHolder(PickStorageImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public CameraPreviewLayoutBinding binding;

        public HeaderViewHolder(CameraPreviewLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }
}