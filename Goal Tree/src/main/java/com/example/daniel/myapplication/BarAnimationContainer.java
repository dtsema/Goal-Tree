package com.example.daniel.myapplication;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.ImageView;

import java.lang.ref.SoftReference;
import java.util.ArrayList;


public class BarAnimationContainer {
    private class AnimationFrame{
        private int mResourceId;
        int mDuration;


        AnimationFrame(int resourceId, int duration){
            mResourceId = resourceId;
            mDuration = duration;

        }
        int getResourceId() {
            return mResourceId;
        }
        int getDuration() {
            return mDuration;
        }

    }
    private ArrayList<AnimationFrame> mAnimationFrames;
    private int mIndex;
    private boolean newDuration;
    private boolean growthDone;
    private boolean mShouldRun;
    private boolean mIsRunning;
    private SoftReference<ImageView> mSoftReferenceImageView;
    private Handler mHandler;


    private Bitmap mRecycleBitmap;


    private OnAnimationStoppedListener mOnAnimationStoppedListener;
    private OnAnimationFrameChangedListener mOnAnimationFrameChangedListener;

    private BarAnimationContainer(ImageView imageView) {
        init(imageView);
    }

    private static BarAnimationContainer sInstance;

    static BarAnimationContainer getInstance(ImageView imageView) {
        if (sInstance == null)
            sInstance = new BarAnimationContainer(imageView);
        sInstance.mRecycleBitmap = null;
        return sInstance;
    }


    private void init(ImageView imageView){
        mAnimationFrames = new ArrayList<>();
        mSoftReferenceImageView = new SoftReference<>(imageView);

        mHandler = new Handler();
        if(mIsRunning){
            stop();
        }

        mShouldRun = false;
        mIsRunning = false;

        mIndex = -1;
    }

    void addAllFrames(int[] resIds, int interval){
        for(int resId : resIds){
            mAnimationFrames.add(new AnimationFrame(resId, interval));
        }
    }

    void removeAllFrames(){
        mAnimationFrames.clear();
    }

    private AnimationFrame getNext() {

        mIndex++;
        if (mIndex >= mAnimationFrames.size()) {
            mIndex = mAnimationFrames.size()-1;
        }
        return mAnimationFrames.get(mIndex);
    }
    void setIndex(int index){
        mIndex = index;
    }

    public interface OnAnimationStoppedListener{
         void onAnimationStopped();
    }

    public interface OnAnimationFrameChangedListener{
         void onAnimationFrameChanged(int index);
    }

    public synchronized void start() {
        mShouldRun = true;
        if (mIsRunning)
            return;
        mHandler.post(new FramesSequenceAnimation());
    }

    public synchronized void stop() {
        mShouldRun = false;
    }

    private class FramesSequenceAnimation implements Runnable{

        @Override
        public void run() {
            ImageView imageView = mSoftReferenceImageView.get();
            if (!mShouldRun || imageView == null) {
                mIsRunning = false;
                if (mOnAnimationStoppedListener != null) {
                    mOnAnimationStoppedListener.onAnimationStopped();
                }
                return;
            }
            mIsRunning = true;

            AnimationFrame frame = getNext();
            GetImageDrawableTask task = new GetImageDrawableTask(imageView);
            task.execute(frame.getResourceId());
            int duration;
            if (!newDuration) {duration = frame.getDuration();}
            else duration = 60;
            mHandler.postDelayed(this, duration);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetImageDrawableTask extends AsyncTask<Integer, Void, Drawable>{
        private ImageView mImageView;

        GetImageDrawableTask(ImageView imageView) {
            mImageView = imageView;
        }

        @SuppressLint("NewApi")
        @Override
        protected Drawable doInBackground(Integer... params) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            if (mRecycleBitmap != null)
                options.inBitmap = mRecycleBitmap;
            mRecycleBitmap = BitmapFactory.decodeResource(mImageView
                    .getContext().getResources(), params[0], options);
            return new BitmapDrawable(mImageView.getContext().getResources(),mRecycleBitmap);
        }

        @Override
        protected void onPostExecute(Drawable result) {
            super.onPostExecute(result);
            if(result!=null) mImageView.setImageDrawable(result);
            if (mOnAnimationFrameChangedListener != null)
                mOnAnimationFrameChangedListener.onAnimationFrameChanged(mIndex);
        }

    }
}