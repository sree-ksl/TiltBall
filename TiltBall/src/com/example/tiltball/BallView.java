package com.example.tiltball;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class BallView extends View{
	
	public float X;
	public float Y;
	private final int R;
	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

	public BallView(Context context, float x, float y, int r) {
		super(context);
		paint.setColor(0xFF00FF00);
		this.X = x;
		this.Y = y;
		this.R = r;
		
	}
	
	@Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(X, Y, R, paint);
    } 

}
