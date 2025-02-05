package com.example.journal;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.view.View;

public class PenWritingView extends View {

    private Paint paint;
    private Path path;
    private PathMeasure pathMeasure;
    private float length;
    private float currentLength;

    public PenWritingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.red)); // Pen color (black)
        paint.setStrokeWidth(10); // Pen width
        paint.setStyle(Paint.Style.STROKE); // Drawing as a stroke (not filled)

        path = new Path();
        path.moveTo(100, 100); // Start point of the line
        path.lineTo(800, 100); // End point of the line (horizontal line)

        pathMeasure = new PathMeasure(path, false);
        length = pathMeasure.getLength(); // Get the length of the path
        currentLength = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Create a new path for the current drawing progress
        Path partialPath = new Path();
        pathMeasure.getSegment(0, currentLength, partialPath, true);

        // Draw the partial path to the canvas
        canvas.drawPath(partialPath, paint);
    }

    // Method to start the writing animation
    public void startAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(0, length);
        animator.setDuration(350); // 1 second for the full line animation
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentLength = (float) animation.getAnimatedValue();
                invalidate(); // Redraw the view
            }
        });
        animator.start();
    }
}
