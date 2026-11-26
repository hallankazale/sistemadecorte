package com.cortaclip.app;
import android.app.Activity;
import android.os.Bundle;
import android.content.*;
import android.net.Uri;
import android.provider.MediaStore;
import android.media.MediaMetadataRetriever;
import android.widget.*;
import android.graphics.Color;
import android.view.View;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.transformer.*;
import java.io.*;
@UnstableApi
public class MainActivity extends Activity {
 private Uri source; private long duration, start; private int count; private boolean busy; private TextView status; private Button pick, run; private Transformer transformer;
 @Override public void onCreate(Bundle b){super.onCreate(b); LinearLayout layout=new LinearLayout(this);layout.setOrientation(1);layout.setPadding(32,64,32,32);layout.setBackgroundColor(Color.rgb(18,20,30));TextView title=new TextView(this);title.setText("CortaClip AI");title.setTextSize(30);title.setTextColor(Color.WHITE);layout.addView(title);status=new TextView(this);status.setText("Selecione um vídeo para gerar cortes de até 60 segundos.");status.setTextColor(Color.WHITE);status.setPadding(0,32,0,32);layout.addView(status);pick=new Button(this);pick.setText("Importar vídeo");layout.addView(pick);run=new Button(this);run.setText("Gerar cortes de 60 segundos");run.setEnabled(false);layout.addView(run);setContentView(layout);pick.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("video/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,10);});run.setOnClickListener(v->{busy=true;pick.setEnabled(false);run.setEnabled(false);start=0;count=0;next();});}
 @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r!=10||c!=RESULT_OK||d==null)return;source=d.getData();try(MediaMetadataRetriever m=new MediaMetadataRetriever()){m.setDataSource(this,source);duration=Long.parseLong(m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));if(duration<=0)throw new IOException();status.setText("Vídeo selecionado: "+duration/1000+" segundos");run.setEnabled(true);}catch(Exception e){run.setEnabled(false);status.setText("Não foi possível ler o vídeo.");}}
 private void finishJob(String message){busy=false;pick.setEnabled(true);run.setEnabled(source!=null);status.setText(message);}
 private void next(){if(start>=duration){finishJob(count+" cortes salvos na galeria.");return;}long end=Math.min(duration,start+60000);File temp=new File(getCacheDir(),"clip-"+System.nanoTime()+".mp4");status.setText("Exportando corte "+(count+1)+" de "+((duration+59999)/60000));MediaItem item=new MediaItem.Builder().setUri(source).setClippingConfiguration(new MediaItem.ClippingConfiguration.Builder().setStartPositionMs(start).setEndPositionMs(end).build()).build();try{transformer=new Transformer.Builder(this).setVideoMimeType("video/avc").setAudioMimeType("audio/mp4a-latm").addListener(new Transformer.Listener(){@Override public void onCompleted(Composition composition,ExportResult result){new Thread(()->{try{save(temp);runOnUiThread(()->{count++;start=end;next();});}catch(Exception e){runOnUiThread(()->finishJob("Erro ao salvar: "+e.getMessage()));}finally{temp.delete();}}).start();}@Override public void onError(Composition composition,ExportResult result,ExportException error){temp.delete();finishJob("Erro de exportação: "+error.getMessage());}}).build();transformer.start(new EditedMediaItem.Builder(item).build(),temp.getAbsolutePath());}catch(Exception e){temp.delete();finishJob("Erro: "+e.getMessage());}}
 private void save(File file)throws IOException{ContentValues values=new ContentValues();values.put(MediaStore.Video.Media.DISPLAY_NAME,"CortaClip_"+System.currentTimeMillis()+".mp4");values.put(MediaStore.Video.Media.MIME_TYPE,"video/mp4");values.put(MediaStore.Video.Media.RELATIVE_PATH,"Movies/CortaClip");values.put(MediaStore.Video.Media.IS_PENDING,1);Uri uri=getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,values);if(uri==null)throw new IOException("Sem acesso à galeria");try{try(InputStream in=new FileInputStream(file);OutputStream out=getContentResolver().openOutputStream(uri)){if(out==null)throw new IOException("Sem espaço ou acesso");byte[] buffer=new byte[65536];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);}values.clear();values.put(MediaStore.Video.Media.IS_PENDING,0);getContentResolver().update(uri,values,null,null);}catch(Exception e){getContentResolver().delete(uri,null,null);throw new IOException(e);}}
}
