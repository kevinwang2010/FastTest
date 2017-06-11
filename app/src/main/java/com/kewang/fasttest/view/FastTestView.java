package com.kewang.fasttest.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.kewang.fasttest.listener.OnVerifyResultCallback;
import com.kewwang.fasttest.R;

import java.util.Random;

/**
 * 滑动图形验证码
 * Created by AD on 2016/8/29.
 */
public class FastTestView extends View {

    private Context mContext;
    private Paint mPaint;
    private Paint mSeekBarPaint;
    private int downX = 0;
    private int downY = 0;

    /**主图宽度*/
    private int mMainImageViewWidth = 0;
    /**主图高度*/
    private int mMainImageViewHeight = 0;

    /**seekbar区域背景的宽度*/
    private int mSeekBarBgWidth = 0;
    /**seekbar区域背景的高度*/
    private int mSeekBarBgHeight = 0;
    /**seekbar宽度*/
    private int mSeekBarWidth = 0;
    /**seekbar高度*/
    private int mSeekBarHeight = 0;

    /**seekbar距离左边的偏移量*/
    private int mSliderOffsetX = 0;

    /**seekbar滑块滑动的距离*/
    private int mBarMoveX = 0;

    /**抠图小块的宽度*/
    private int mMattingImageWidth;

    /**抠图小块的高度*/
    private int mMattingImageHeight;

    /**seekbar距离左边距*/
    private int mSeekBarMarginLeft = 30;

    /**seekbar的滑块区域是否被touch*/
    private boolean isTouchSeekBarSlider = false;

    /**手机屏幕宽度*/
    private int mScreenWidth;
    /**手机屏幕高度*/
    private int mScreenHeight;

    /**抠图小图片与抠图区域的偏差值在正负mVerifyOffsetX范围内说明验证通过，通过调节这个值来调节验证精度*/
    private int mVerifyOffsetX = 5;

    /**主图片上抠图的x位置*/
    private int mMainImageViewMattingPositionX;
    /**主图片上抠图的y位置*/
    private int mMainImageViewMattingPositionY;

    /**验证结束*/
    private boolean isVerifyFinish = false;
    /**验证结果*/
    private boolean isVerifySuccess = false;

    /**刷新按钮*/
    private int mReloadButtonWidth = 100;
    /**刷新按钮是否被点击*/
    private boolean isReloadButtonClick = false;
    /**主图片资源ID*/
    private int mMainImageViewResID = 0;

    /**验证失败错误次数*/
    private int mVerifyErrorTimesCount = 0;

    /**验证开始时间*/
    private long mVerifyStartTime;
    private long mVerifySpendTime;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private OnVerifyResultCallback mOnVerifyResultCallback = null;

    public FastTestView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public FastTestView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public FastTestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(1);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);

        mSeekBarPaint = new Paint();
        mSeekBarPaint.setAntiAlias(true);
        mSeekBarPaint.setStrokeWidth(1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        //获取屏幕信息
        wm .getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int mWidth = MeasureSpec.getSize(widthMeasureSpec);
        int mHeight = MeasureSpec.getSize(heightMeasureSpec);

        mMainImageViewWidth = mWidth > mScreenWidth ? mScreenWidth : mWidth;
        mMainImageViewHeight = mMainImageViewWidth/2;

        changeMainImageView();
        initParams();

        setMeasuredDimension(mMainImageViewWidth, mMainImageViewHeight+mSeekBarBgHeight);
    }

    /**初始化相关参数*/
    private void initParams(){
        mSeekBarBgWidth = mMainImageViewWidth;
        mSeekBarBgHeight = mMainImageViewHeight/2;

        mSeekBarWidth = mMainImageViewWidth - mSeekBarMarginLeft*2;
        mSeekBarHeight = mSeekBarBgHeight/2;
        mSliderOffsetX = (mMainImageViewWidth-mSeekBarWidth)/2;

        mMainImageViewMattingPositionX = getRandomValue(mMainImageViewWidth-2*mMattingImageWidth-2*mSeekBarMarginLeft)+mMattingImageWidth+mSeekBarMarginLeft;
        mMainImageViewMattingPositionY = getRandomValue(mMainImageViewHeight -mMattingImageHeight);

        downX = 0;
        downY = 0;
        mBarMoveX = 0;

        isVerifyFinish = false;
        isVerifySuccess = false;

        isReloadButtonClick = false;

        mVerifyErrorTimesCount = 0;

        mMattingImageWidth = mMainImageViewHeight/5;
        mMattingImageHeight = mMattingImageWidth;
    }

    private void refreshParams(){
        initParams();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /**刷新按钮rect*/
        RectF mReloadButtonRect = new RectF(mMainImageViewWidth-mReloadButtonWidth, 0 , mMainImageViewWidth, mReloadButtonWidth);
        /**主图片rect*/
        RectF mMainBgRect = new RectF(0, 0 , mMainImageViewWidth, mMainImageViewHeight);
        /**抠图小块rect*/
        RectF mSliderRect = new RectF(0+mBarMoveX-mMainImageViewMattingPositionX+mSeekBarMarginLeft, 0 , mMainImageViewWidth+mBarMoveX-mMainImageViewMattingPositionX+mSeekBarMarginLeft, mMainImageViewHeight);
        /**seekbar区域*/
        RectF mSliderStrokeRect = new RectF(mBarMoveX+mSeekBarMarginLeft, mMainImageViewMattingPositionY , mBarMoveX+mMattingImageWidth+mSeekBarMarginLeft, mMainImageViewMattingPositionY+mMattingImageHeight);

        /**background主图片区域的方格背景图，在抠图的区域可以看到这个背景图*/
        canvas.drawBitmap(getBitmap(mContext.getResources().getDrawable(R.drawable.icon_main_bg_1)),null,mMainBgRect,mPaint);

        /**main background主图片*/
        canvas.drawBitmap(getXFramBitmap(PorterDuff.Mode.SRC_OUT),null,mMainBgRect,mPaint);

        /**抠图小块图片slider*/
        canvas.drawBitmap(getXFramBitmap(PorterDuff.Mode.SRC_IN),null,mSliderRect,mPaint);

        /**抠图小块图片slider的醒目边框*/
        canvas.drawBitmap(getBitmap(mContext.getResources().getDrawable(R.drawable.icon_cut_out_bg_3)),null,mSliderStrokeRect,mPaint);

        /**seekbar*/
        drawSeekBar(canvas);

        /**reload 按钮*/
        canvas.drawBitmap(getBitmap(mContext.getResources().getDrawable(R.drawable.icon_reload)),null,mReloadButtonRect,mPaint);

        /**当次验证是否结束*/
        if(isVerifyFinish){
            if(isVerifySuccess){
                canvas.drawBitmap(getBitmap(mContext.getResources().getDrawable(R.drawable.icon_success)),null,mMainBgRect,mPaint);
                int s1 = (int) (mVerifySpendTime/1000);
                int s2 = (int) (mVerifySpendTime%1000/100);
                int percent = getRandomValue(100)+1;
                onDrawText(canvas,s1+"."+s2+"秒的速度超过"+percent+"%的用户",0, mMainImageViewHeight/2 , mMainImageViewWidth, mMainImageViewHeight*3/4);
            }else{
                canvas.drawBitmap(getBitmap(mContext.getResources().getDrawable(R.drawable.icon_fail)),null,mMainBgRect,mPaint);
            }
        }
   }

    /**seekbar区域的图形绘制*/
    private void drawSeekBar(Canvas canvas){
        mSeekBarPaint.setStrokeWidth(1);
        /**sliderbg  seekbar背景区域*/
        mSeekBarPaint.setColor(Color.parseColor("#4a5a6a"));
        RectF mSliderBgRect = new RectF(0, mMainImageViewHeight, mSeekBarBgWidth, mMainImageViewHeight+mSeekBarBgHeight);
        canvas.drawRect(mSliderBgRect,mSeekBarPaint);

        /**sliderbgview seekbar条状区域*/
        mSeekBarPaint.setColor(Color.parseColor("#ffffff"));
        canvas.drawCircle(mSeekBarHeight/2+mSliderOffsetX, mMainImageViewHeight+mSeekBarBgHeight/2, mSeekBarHeight/2,mSeekBarPaint);
        canvas.drawCircle(mSeekBarWidth-mSeekBarHeight/2+mSliderOffsetX, mMainImageViewHeight+mSeekBarBgHeight/2, mSeekBarHeight/2,mSeekBarPaint);
        RectF rect = new RectF(mSeekBarHeight/2+mSliderOffsetX, mMainImageViewHeight+mSeekBarBgHeight/2-mSeekBarHeight/2, mSeekBarWidth-mSeekBarHeight/2+mSliderOffsetX, mMainImageViewHeight+mSeekBarBgHeight/2+mSeekBarHeight/2);
        canvas.drawRect(rect,mSeekBarPaint);

        /**sliderbar seekbar滑动块*/
        mSeekBarPaint.setColor(Color.parseColor("#595954"));
        canvas.drawCircle(mSeekBarHeight/2+mSliderOffsetX+mBarMoveX, mMainImageViewHeight+mSeekBarBgHeight/2,mSeekBarHeight/2,mSeekBarPaint);

        /**seekbar滑动块上画三条线*/
        mSeekBarPaint.setColor(Color.parseColor("#ffffff"));
        mSeekBarPaint.setStrokeWidth(3);
        canvas.drawLine(mSeekBarHeight/2+mSliderOffsetX+mBarMoveX-mSeekBarHeight/4, mMainImageViewHeight+mSeekBarBgHeight/2-mSeekBarHeight/4,mSeekBarHeight/2+mSliderOffsetX+mBarMoveX-mSeekBarHeight/4, mMainImageViewHeight+mSeekBarBgHeight/2+mSeekBarHeight/4,mSeekBarPaint);
        canvas.drawLine(mSeekBarHeight/2+mSliderOffsetX+mBarMoveX, mMainImageViewHeight+mSeekBarBgHeight/2-mSeekBarHeight/4,mSeekBarHeight/2+mSliderOffsetX+mBarMoveX, mMainImageViewHeight+mSeekBarBgHeight/2+mSeekBarHeight/4,mSeekBarPaint);
        canvas.drawLine(mSeekBarHeight/2+mSliderOffsetX+mBarMoveX+mSeekBarHeight/4, mMainImageViewHeight+mSeekBarBgHeight/2-mSeekBarHeight/4,mSeekBarHeight/2+mSliderOffsetX+mBarMoveX+mSeekBarHeight/4, mMainImageViewHeight+mSeekBarBgHeight/2+mSeekBarHeight/4,mSeekBarPaint);
    }

    /**触摸、点击事件的处理*/
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getX();
                downY = (int) event.getY();
                if(isClickReloadButtonRect(downX,downY)){
                    isReloadButtonClick = true;
                }
                if(isClickSeekBarSliderRect(downX,downY)){
                    isTouchSeekBarSlider = true;
                    mVerifyStartTime = System.currentTimeMillis();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) event.getX();
                int moveY = (int) event.getY();
                if(isTouchSeekBarSlider){
                    mBarMoveX += moveX - downX;
                    if(mBarMoveX<0){
                        mBarMoveX = 0;
                    }else if(mBarMoveX>mSeekBarWidth-mSeekBarHeight){
                        mBarMoveX = mSeekBarWidth-mSeekBarHeight;
                    }else{
                        downX = moveX;
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if(isTouchSeekBarSlider){
                    verifyResultCheck();
                }

                if(isClickReloadButtonRect(downX,downY) && isReloadButtonClick){
                    isReloadButtonClick = false;
                    reload();
                }
                break;
        }
        return true;
    }

    /**设置seekbar的滑块回到原始位置*/
    private void scrollSeekBarToOrignal(){
        if(Build.VERSION.SDK_INT < 11) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (mBarMoveX > 0) {
                        SystemClock.sleep(1);
                        mBarMoveX = mBarMoveX - 2;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                invalidate();
                            }
                        });
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            invalidate();
                        }
                    });
                }
            }).start();
        }else{
            final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                int tmp = mBarMoveX;
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        float mAnimatorValue = (float) valueAnimator.getAnimatedValue();
                        mBarMoveX = (int) ((1-mAnimatorValue)*tmp);
                    }
                    invalidate();
                }
            });
            if(mBarMoveX > mSeekBarWidth*3/4 && mSeekBarWidth <= mSeekBarWidth){
                valueAnimator.setDuration(200);
            }else if(mBarMoveX > mSeekBarWidth*1/2 && mSeekBarWidth <= mSeekBarWidth*3/4){
                valueAnimator.setDuration(150);
            }else if(mBarMoveX > mSeekBarWidth/4 && mBarMoveX <= mSeekBarWidth*1/2){
                valueAnimator.setDuration(100);
            }else{
                valueAnimator.setDuration(50);
            }
            valueAnimator.setRepeatCount(0);
            valueAnimator.start();
        }
    }

    /**通过设置setXfermode获取抠图的效果图片*/
    private Bitmap getXFramBitmap(PorterDuff.Mode mode){
        Bitmap bitmap = getBitmap(mContext.getResources().getDrawable(mMainImageViewResID));
        Bitmap mask = getBitmap(mContext.getResources().getDrawable(R.drawable.icon_cut_out_bg_2));
        //圆形图片宽高
        int width = mMainImageViewWidth;
        int height = mMainImageViewHeight;
        //构建一个bitmap
        Bitmap backgroundBmp = Bitmap.createBitmap(width,
                height, Bitmap.Config.ARGB_8888);
        //new一个Canvas，在backgroundBmp上画图
        Canvas mCanvas = new Canvas(backgroundBmp);
        Paint paint = new Paint();
        //设置边缘光滑，去掉锯齿
        paint.setAntiAlias(true);
        //宽高相等，即正方形
        RectF rect = new RectF(0, 0, width, height);
        RectF rectMask = new RectF(mMainImageViewMattingPositionX, mMainImageViewMattingPositionY, mMainImageViewMattingPositionX+mMattingImageWidth, mMainImageViewMattingPositionY+mMattingImageHeight);
        mCanvas.drawBitmap(mask,null,rectMask,paint);
        //设置当两个图形相交时的模式，SRC_IN为取SRC图形相交的部分，多余的将被去掉
        paint.setXfermode(new PorterDuffXfermode(mode));
        //canvas将bitmap画在backgroundBmp上
        mCanvas.drawBitmap(bitmap, null, rect, paint);
        //返回已经绘画好的backgroundBmp
        return backgroundBmp;
    }

    private Bitmap getBitmap(Drawable drawable){
        BitmapDrawable bitmapDrawable = (BitmapDrawable)drawable;
        return bitmapDrawable.getBitmap();
    }

    /**获取随机数，伪随机*/
    private int getRandomValue(int number){
        Random r = new Random();
        return r.nextInt(number);
    }

    /**seekbar的滑块的区域是否被点击*/
    private boolean isClickSeekBarSliderRect(int downX,int downY){
        if(downX > mSeekBarHeight/2+mSliderOffsetX+mBarMoveX-mSeekBarHeight/2
                && downX < mSeekBarHeight/2+mSliderOffsetX+mBarMoveX+mSeekBarHeight/2
                && downY > mMainImageViewHeight+mSeekBarBgHeight/2-mSeekBarHeight/2
                && downY < mMainImageViewHeight+mSeekBarBgHeight/2+mSeekBarHeight/2
                ){
            return true;
        }
        return false;
    }

    /**判断刷新按钮区域是否被点击*/
    private boolean isClickReloadButtonRect(int downX,int downY){
        if(downX > mMainImageViewWidth-mReloadButtonWidth
                && downX < mMainImageViewWidth
                && downY > 0
                && downY < mReloadButtonWidth
                ){
            return true;
        }
        return false;
    }

    /**抠图小图片是否与主图的抠图区域重合*/
    private boolean isMattingImageCoincideWithMainImageViewMattingRect(){
        if(mMainImageViewMattingPositionX - (mBarMoveX + mSeekBarMarginLeft) < mVerifyOffsetX
                && mMainImageViewMattingPositionX - (mBarMoveX + mSeekBarMarginLeft) > -mVerifyOffsetX){
            return true;
        }
        return false;
    }

    /**图形验证码是否验证通过*/
    private boolean isVerifySuccess(){
        if(isMattingImageCoincideWithMainImageViewMattingRect()){
            return true;
        }
        return false;
    }

    /**验证结果的检测及处理*/
    private void verifyResultCheck(){
        isTouchSeekBarSlider = false;
        isVerifyFinish = true;
        if(isVerifySuccess()){
            /**验证通过*/
            isVerifySuccess = true;
            mVerifySpendTime = System.currentTimeMillis() - mVerifyStartTime;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mOnVerifyResultCallback != null){
                        mOnVerifyResultCallback.onSuccess();
                    }
                }
            },2000);
        }else{
            mVerifyErrorTimesCount++;
            /**验证失败*/
            isVerifySuccess = false;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mOnVerifyResultCallback != null){
                        mOnVerifyResultCallback.onFail();
                    }

                    isVerifyFinish = false;
                    /**seekbar置零*/
                    scrollSeekBarToOrignal();

                    /**验证失败3次后reload*/
                    if(mVerifyErrorTimesCount == 3){
                        mVerifyErrorTimesCount = 0;
                        reload();
                    }
                }
            },1000);
        }
        invalidate();
    }

    /**刷新*/
    private void reload(){
        refreshParams();
        changeMainImageView();
        invalidate();
    }

    /**在图片集合中随机获取*/
    private int getRandomDrawableResID(){
        return mDrawableResID[getRandomValue(6)];
    }

    /**主图图片资源集合*/
    private int mDrawableResID[] = {
            R.drawable.icon_main_bg_01,
            R.drawable.icon_main_bg_02,
            R.drawable.icon_main_bg_03,
            R.drawable.icon_main_bg_04,
            R.drawable.icon_main_bg_05,
            R.drawable.icon_main_bg_06
    };

    /**修改主图图片资源*/
    private void changeMainImageView(){
        mMainImageViewResID = getRandomDrawableResID();
    }

    /**设置验证结果的回调*/
    public void setOnVerifyResultCallback(OnVerifyResultCallback callback){
        this.mOnVerifyResultCallback = callback;
    }

    private void onDrawText(Canvas canvas,String text,int startx,int starty,int endx,int endy){
        Rect targetRect = new Rect(startx, starty, endx, endy);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(5);
        paint.setTextSize(mMainImageViewWidth/text.length()-10);
        paint.setColor(Color.parseColor("#aaffffff"));
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        int baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
        // 下面这行是实现水平居中，drawText对应改为传入targetRect.centerX()
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(text, targetRect.centerX(), baseline, paint);
    }
}
