package com.cesecsh.statusframelayout.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;

/**
 * StatusFrameLayout
 * Created by RockQ on 2017/3/6.
 */

public class PlayBallLoadingView extends BaseLoadView {
    private Paint mPaint, mPaintCircle, mPaintBall;
    private float mPaintStrokeWidth;
    private float mHigh = 0f;
    private float mWidth = 0f;
    private float quadToStart = 0f;
    private float mRadius = 0f;
    private float mRadiusBall = 0f;
    private float ballY = 0f;
    Path path = new Path();

    public PlayBallLoadingView(Context context) {
        super(context);
        setViewColor(Color.GREEN);
        setBallColor(Color.RED);
        startAnim();
    }

    public PlayBallLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setViewColor(Color.GREEN);
        setBallColor(Color.RED);
        startAnim();
    }

    public PlayBallLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setViewColor(Color.GREEN);
        setBallColor(Color.RED);
        startAnim();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mHigh = getMeasuredHeight();
        mWidth = getMeasuredWidth();
        quadToStart = mHigh / 2;
        mRadius = dip2px(5);
        mPaintStrokeWidth = 2;
        ballY = mHigh / 2;
        mRadiusBall = dip2px(4);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        path = new Path();
        path.moveTo(0 + mRadius * 2 + mPaintStrokeWidth, getMeasuredHeight() / 2);
        path.quadTo(mWidth / 2, quadToStart, mWidth - mRadius * 2 - mPaintStrokeWidth, mHigh / 2);
        mPaint.setStrokeWidth(3);
        canvas.drawPath(path, mPaint);


        mPaintCircle.setStrokeWidth(mPaintStrokeWidth);
        canvas.drawCircle(mRadius + mPaintStrokeWidth, mHigh / 2, mRadius, mPaintCircle);
        canvas.drawCircle(mWidth - mRadius - mPaintStrokeWidth, mHigh / 2, mRadius, mPaintCircle);


        if (ballY - mRadiusBall > mRadiusBall) {
            canvas.drawCircle(mWidth / 2, ballY - mRadiusBall, mRadiusBall, mPaintBall);
        } else {
            canvas.drawCircle(mWidth / 2, mRadiusBall, mRadiusBall, mPaintBall);

        }

    }


    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);


        mPaintCircle = new Paint();
        mPaintCircle.setAntiAlias(true);
        mPaintCircle.setStyle(Paint.Style.STROKE);
        mPaintCircle.setColor(Color.WHITE);


        mPaintBall = new Paint();
        mPaintBall.setAntiAlias(true);
        mPaintBall.setStyle(Paint.Style.FILL);
        mPaintBall.setColor(Color.WHITE);


    }


    public void setViewColor(int color) {
        mPaint.setColor(color);
        mPaintCircle.setColor(color);
        postInvalidate();
    }

    public void setBallColor(int color) {
        mPaintBall.setColor(color);
        postInvalidate();
    }


    @Override
    protected void InitPaint() {
        initPaint();
    }

    @Override
    protected void OnAnimationUpdate(ValueAnimator valueAnimator) {
        float value = (float) valueAnimator.getAnimatedValue();

        if (value > 0.75) {


            quadToStart = mHigh / 2 - (1f - (float) valueAnimator.getAnimatedValue()) * mHigh / 3f;
        } else {
            quadToStart = mHigh / 2 + (1f - (float) valueAnimator.getAnimatedValue()) * mHigh / 3f;

        }

        if (value > 0.35f) {
            ballY = mHigh / 2 - (mHigh / 2 * value);
        } else {
            ballY = mHigh / 2 + (mHigh / 6 * value);
        }


        invalidate();
    }

    @Override
    protected void OnAnimationRepeat(Animator animation) {

    }

    @Override
    protected int OnStopAnim() {
        quadToStart = mHigh / 2;
        ballY = mHigh / 2;
        return 0;
    }

    @Override
    protected int SetAnimRepeatMode() {
        return ValueAnimator.REVERSE;
    }

    @Override
    protected void AinmIsRunning() {

    }

    @Override
    protected int SetAnimRepeatCount() {
        return ValueAnimator.INFINITE;
    }
}
