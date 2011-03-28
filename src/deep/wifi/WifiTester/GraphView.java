package deep.wifi.WifiTester;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class GraphView extends View {
	
	WifiTester app;
	List<Integer> lRssi = new ArrayList<Integer>();
	
	public GraphView(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);

	}
	
	public GraphView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	}
	
	public GraphView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public void init(){
		for (int i=0; i<61; i++) {
	    	lRssi.add(-90);
	    }
	}
	
	public void setApp(WifiTester wapp) {
		app = wapp;
	}
	
	public void update() {
		app.updateWifiDetails();
		if (lRssi.size() > 0) {
			lRssi.remove(0);
		}
		lRssi.add(app.rssi);
		app.mRedrawHandler.sleep(1000);
	}
	
	private float min(float a, float b) {
		if (a<b) return a;
		return b;
	}
	
	private float max(float a, float b) {
		if (a>b) return a;
		return b;
	}
	
   	//Graph bar depicts -90 to -40
	private float rssi_to_pos(float minpos, float maxpos, int rssi) {
		float res = minpos +  (((maxpos-minpos)/100) * ((rssi + 90) * (100/(90-40))));
		res = min(res, maxpos);
		res = max(res, minpos);
		return res;
	}
	
    @Override
    public void onDraw(Canvas canvas) {
    	Paint paint = new Paint();
    	RectF rect = new RectF();
    	int width = getWidth();
    	int height = getHeight();
    	
    	rect.left = 5;
    	rect.top = 5;
    	rect.bottom = height - 5;
    	rect.right = width - 5;
    	
    	paint.setAntiAlias(true);
    	paint.setColor(Color.rgb(20,20,20));
    	
    	canvas.drawRoundRect(rect, (float)10.1, (float)10.1, paint);
    	
    	paint.setColor(Color.WHITE);
    	paint.setTextSize(17);//getResources().getDimensionPixelSize(15));
    	String s = "Current Signal Strength : ";
    	s += Integer.toString(app.rssi);
    	canvas.drawText(s, 15, 25, paint);
    	
    	rect.top = 35;
    	rect.left = 15;
    	rect.bottom = height/3 - 15;
    	rect.right = width - 15;
    	paint.setStyle(Paint.Style.STROKE);
    	
    	paint.setColor(Color.GREEN);
    	canvas.drawRect(rect, paint);
    	
    	rect.top = 45;
    	rect.left = 25;
    	rect.bottom = height/3 - 25;
    	float avail = width - 25;
    	
    	rect.right = rssi_to_pos(rect.left, avail, app.rssi);
    	
    	//Color is anywhere between deep red to deep green.. 
    	paint.setStyle(Paint.Style.FILL);
    	int green =  (int)  rssi_to_pos(0, 255, app.rssi);
    	paint.setColor(Color.rgb(255-green, green, 0));
    	canvas.drawRect(rect, paint);
    	
    	paint.setStyle(Paint.Style.STROKE);
    	
    	rect.top = height/3;
    	rect.left = 15;
    	rect.bottom = height - 15;
    	rect.right = width - 15;
    	
    	float hmax = rect.bottom - rect.top;
    	float wmax = rect.right - rect.left;
    	float[] pts = new float[240];
    	
    	
    	
    	int j=0;
    	for(int i=-90; i<-31; i+=10) {
    		int pos = j*4;
    		pts[pos] = rect.left - 5;
    		pts[pos+1] = rect.top + (hmax - rssi_to_pos(0, hmax, i));
    		pts[pos+2] = rect.left;
    		pts[pos+3] = rect.top + (hmax - rssi_to_pos(0, hmax, i));
    		j+=1;
    	}
    	
    	paint.setColor(Color.YELLOW);
    	canvas.drawLines(pts, paint);
    	
    	for (int i=0; i< 60; i++) {
    		int pos = i*4;
    		pts[pos] = rect.left + (wmax / 60) * (i);
    		pts[pos+1] = rect.top + (hmax - rssi_to_pos(0, hmax, lRssi.get(i)));
    		pts[pos+2] = rect.left + (wmax / 60) * (i+1);
    		pts[pos+3] = rect.top + (hmax - rssi_to_pos(0, hmax, lRssi.get(i+1)));
    	}
    	
    	paint.setColor(Color.rgb(255, 117, 117));
    	canvas.drawLines(pts, paint);
    	
    	paint.setColor(Color.YELLOW);
    	canvas.drawRect(rect, paint);
    }

}
