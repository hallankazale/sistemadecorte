package com.cortaclip.app;
import android.content.Context;
import android.graphics.*;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
public final class ThumbnailEngine {
 private ThumbnailEngine(){}
 public static Bitmap render(Context context,Uri uri,String title,boolean vertical){
 int w=vertical?720:1280,h=vertical?1280:720;
 Bitmap result=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
 Canvas canvas=new Canvas(result);Paint p=new Paint(3);canvas.drawColor(0xff171329);
 try(MediaMetadataRetriever retriever=new MediaMetadataRetriever()){
 retriever.setDataSource(context,uri);
 long duration=Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
 Bitmap frame=retriever.getScaledFrameAtTime(duration*350,MediaMetadataRetriever.OPTION_CLOSEST_SYNC,720,720);
 if(frame!=null){float scale=Math.max((float)w/frame.getWidth(),(float)h/frame.getHeight());float fw=frame.getWidth()*scale,fh=frame.getHeight()*scale;canvas.drawBitmap(frame,null,new RectF((w-fw)/2,(h-fh)/2,(w+fw)/2,(h+fh)/2),p);frame.recycle();}
 }catch(Exception e){result.recycle();throw new IllegalStateException("Não foi possível obter o frame",e);}
 p.setShader(new LinearGradient(0,h*.35f,0,h,new int[]{0x00171329,0xee171329},null,Shader.TileMode.CLAMP));canvas.drawRect(0,0,w,h,p);p.setShader(null);
 p.setColor(0xffb49aff);canvas.drawRect(w*.06f,h*.68f,w*.24f,h*.69f,p);
 android.text.TextPaint tp=new android.text.TextPaint(3);tp.setColor(Color.WHITE);tp.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));tp.setTextSize(w*.065f);tp.setShadowLayer(5,2,3,Color.BLACK);
 android.text.StaticLayout layout=android.text.StaticLayout.Builder.obtain(title,0,title.length(),tp,(int)(w*.88f)).setMaxLines(3).setEllipsize(android.text.TextUtils.TruncateAt.END).build();canvas.save();canvas.translate(w*.06f,h*.71f);layout.draw(canvas);canvas.restore();return result;
 }
}