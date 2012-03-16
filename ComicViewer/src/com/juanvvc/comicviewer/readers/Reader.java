package com.juanvvc.comicviewer.readers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public abstract class Reader {
	protected String uri=null;
	protected int currentPage = -1;
	public static final int MAX_BITMAP_SIZE=1024;
	
	public void load(String uri) throws ReaderException{
		this.uri = uri;
		// current page is -1, since user didn't turn over the page yet. First thing: call to next()
		this.currentPage = -1;
	}
	public abstract void close();
	public abstract int countPages();
	public abstract Drawable current() throws ReaderException;


	public int currentPage() {
		return this.currentPage;
	}
	
	public void moveTo(int page) {
		this.currentPage = page;
	}
	
	public String getURI(){
		return this.uri;
	}
	
	public Drawable next() throws ReaderException{
		if(this.uri==null)
			return null;
		if(this.currentPage<-1 || this.currentPage>=this.countPages())
			return null;
		this.currentPage += 1;
		return this.current();
	}

	public Drawable prev() throws ReaderException{
		if(this.uri==null)
			return null;
		if(this.currentPage<=0)
			return null;
		this.currentPage -= 1;
		return this.current();
	}
	
	/** COnvert a byte array into a Bitmap
	 * 
	 * 	 This method should be a single line:
		 return new BitmapDrawable(BitmapFactory.decodeByteArray(ba, 0, ba.length);
		 or even:
		 Drawable.createFromStream(new ByteArrayInputStream(ba), "name");
		 These work only with small images. This method manages large images (and they are very usual in comic files) 

		 The last versions of Android have a very annoying feature: graphics are always HW accelerated, bitmaps
		 are always loaded as OPENGL_TEXTURES, and a HW limit applies: MAX_BITMAP_SIZE at most.
		 http://groups.google.com/group/android-developers/browse_thread/thread/2352c776651b6f99
		 Some report (http://stackoverflow.com/questions/7428996/hw-accelerated-activity-how-to-get-opengl-texture-size-limit)
		 that the minimum is 2048. In my device, that does not work. 1024 does. TODO: set the minimum to the screen size.
		 Conclusion: in current devices, you cannot load a bitmap larger (width or height) than MAX_BITMAP_SIZE pixels.
		 Fact: many CBRs use images larger than that. OutOfMemory errors appear.
		 Solution: Options.inSampleSize to the rescue: 
		 1.- load the image information (inJustDecodeBounds=true)
		 2.- read the image size
		 3.- if larger than MAX_BITMAP_SIZE, apply a scale
		 4.- load the image scaled
		
		 Remember: we have to do this with every image because is very common CBR files where pages have different sizes
		 for example, double/single pages.
		 
		 This method is in this class because I think that any reader will find this useful.
		 
	 * @param ba The byte array to convert
	 * @return A Bitmap object
	 */
	protected Bitmap byteArrayToBitmap(byte[] ba){
		Options opts=new Options();
		opts.inSampleSize=1;
		opts.inJustDecodeBounds=true;
		BitmapFactory.decodeByteArray(ba, 0, ba.length, opts);
		// now, set the scale according to the image size: 1, 2, 3...
		opts.inSampleSize = Math.max(opts.outHeight, opts.outWidth)/MAX_BITMAP_SIZE+1;
		//TODO: apply a smart scaler
		opts.inScaled=true;
		// set a high quality scale (did really works?)
		opts.inPreferQualityOverSpeed=true;
		opts.inJustDecodeBounds=false;
		// finally, load the scaled image
		return BitmapFactory.decodeByteArray(ba, 0, ba.length, opts);			
	}
	
}
