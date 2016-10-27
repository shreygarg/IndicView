package com.example.indicview;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.opencv.samples.tutorial2.R;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.droid4you.util.cropimage.CropImage;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

public class MainActivity extends Activity {
	private final String TAG = "OCVSample::Activity";

	private final int VIEW_MODE_RGBA = 0;
	private final int VIEW_MODE_GRAY = 1;
	private final int VIEW_MODE_CANNY = 2;
	private final int VIEW_MODE_FEATURES = 5;

	private int tesstype;
	private Mat mRgba;
	private Mat mIntermediateMat;
	private Mat mGray;
	private Mat processedbitmap;
	Bitmap bitmap1;

	private MenuItem mdefault;
	private MenuItem mfastest;
	private MenuItem mmed;
	private MenuItem mslowest;

	private CameraBridgeViewBase mOpenCvCameraView;
	int MY_REQUEST_CODE1 =55;
	int MY_REQUEST_CODE2 =25;

	int MY_REQUEST_CODE3 =35;

	int MY_REQUEST_CODE4 =45;

	protected String _path;
	protected String _path1;
	protected boolean _taken;
	protected Bitmap bitmap;
	protected Button _button;
	protected Button _button2;
	protected Button _button3;
	protected Button _button4;
	protected Button _button5;
	protected EditText _field;
	protected ImageView _display;
	protected  String gtext;
	//boolean tocnt=true;
	ArrayList<Mat> images;// = new ArrayList<Mat>();
	//Mat list[];
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");

				// Load native library after(!) OpenCV initialization
				System.loadLibrary("lept"); System.loadLibrary("tess");
				System.loadLibrary("mixed_sample");
				// mOpenCvCameraView.enableView();
				if (!OpenCVLoader.initDebug()) {
					// Handle initialization error
					System.exit(1);
				}
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};
	@Override
    public void onPause()
    {
        super.onPause();
    }
	
    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
    }
	public MainActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}
	
	@TargetApi(23)
	private boolean addPermission(List<String> permissionsList, String permission) {
	    if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
	        permissionsList.add(permission);
	        // Check for Rationale Option
	        if (!shouldShowRequestPermissionRationale(permission))
	            return false;
	    }
	    return true;
	}
	@SuppressLint("NewApi")
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
	    switch (requestCode) {
	        case 55:
	            {
	            Map<String, Integer> perms = new HashMap<String, Integer>();
	            // Initial
	            perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
	            perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
	            perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
	            // Fill with results
	            for (int i = 0; i < permissions.length; i++)
	                perms.put(permissions[i], grantResults[i]);
	            // Check for ACCESS_FINE_LOCATION
	            if (perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
	                    && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
	                    && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
	                // All Permissions Granted
	            } else {
	                // Permission Denied
	                Toast.makeText(this, "Some Permission is Denied", Toast.LENGTH_SHORT)
	                        .show();
	            }
	            }
	            break;
	        default:
	            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	    }
	}
	@TargetApi(23)
	private void multipermissions() {
	    List<String> permissionsNeeded = new ArrayList<String>();
	 
	    final List<String> permissionsList = new ArrayList<String>();
	    if (!addPermission(permissionsList, Manifest.permission.CAMERA))
	        permissionsNeeded.add("CAMERA");
	    if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
	        permissionsNeeded.add("Read Storage");
	    if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
	        permissionsNeeded.add("Write Storage");
	 
	    if (permissionsList.size() > 0) {
	        if (permissionsNeeded.size() > 0) {
	            // Need Rationale
	        	requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
	                55);
	        return;
	    }
	    }
	 
	}
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);
		getWindow().getDecorView().setBackgroundColor(Color.rgb(1,1,1));
		tesstype = TessBaseAPI.OEM_DEFAULT;
		images=new ArrayList<Mat>();
		
		_path = Environment.getExternalStorageDirectory() + "/temp.jpg";
		_path1 = Environment.getExternalStorageDirectory() + "/testi.jpg";
		_field = (EditText) findViewById(R.id.editText1);
		_button = (Button) findViewById(R.id.button1);
		_display = (ImageView) findViewById(R.id.imageView1);
		_display.setImageResource(R.drawable.banner);
		if (Build.VERSION.SDK_INT >= 23) {
			multipermissions();
		}
		 String tessroot = Environment.getExternalStorageDirectory().toString()+"/tesseract-ocr";
		File file = new File(tessroot);
		if(!file.isDirectory()) 
			copyAsset("tesseract-ocr");
		_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Globals g = Globals.getInstance();
				g.setData(false);
				File file = new File(_path);
				Uri outputFileUri = Uri.fromFile(file);
				Intent intent = new Intent(
						android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
				startActivityForResult(intent, 3);
			}
		});
		//_button.setBackgroundColor(Color.rgb(220,150,14));
		_button.setTextColor(Color.rgb(8,11,63));
		_button2 = (Button) findViewById(R.id.button2);
		_button2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Globals g = Globals.getInstance();
				g.setData(false);
				// TODO Auto-generated method stub
				try {
					CorrectImage();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		_button2.setTextColor(Color.rgb(8,11,63));
		//_button2.setBackgroundColor(Color.rgb(220,150,14));
		_button3 = (Button) findViewById(R.id.button3);
		_button3.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Globals g = Globals.getInstance();
				g.setData(false);
				//useapi();
				_field.setText("Processing...");
				bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
				OcrUse ocr = new OcrUse();
				ocr.execute();
				
				
			}
		});
		_button3.setTextColor(Color.rgb(8,11,63));
		//_button3.setBackgroundColor(Color.rgb(220,150,14));
		_button4 = (Button) findViewById(R.id.button4);
		_button4.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Globals g = Globals.getInstance();
				g.setData(false);
				Translateclass tc = new  Translateclass();
				 tc.execute();
			}
		});
		
		
		_button4.setTextColor(Color.rgb(8,11,63));
		//_button4.setBackgroundColor(Color.rgb(220,150,14));
		_button5 = (Button) findViewById(R.id.button5);
		_button5.setTextColor(Color.rgb(8,11,63));
		_button5.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Globals g = Globals.getInstance();
				g.setData(true);
				// TODO Auto-generated method stub
				File file = new File(_path);
				Uri outputFileUri = Uri.fromFile(file);
				Intent intent = new Intent(
						android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
				startActivityForResult(intent, 3);
			}
		});
		
	}
	protected void contprocess()
	{
		try {
			CorrectImage();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_field.setText("Processing...");
		bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
		OcrUse ocr = new OcrUse();
		ocr.execute();
	}
	
	private void copyAsset(String path) {
		  AssetManager manager = getAssets();

		  // If we have a directory, we make it and recurse. If a file, we copy its
		  // contents.
		  try {
		    String[] contents = manager.list(path);

		    // The documentation suggests that list throws an IOException, but doesn't
		    // say under what conditions. It'd be nice if it did so when the path was
		    // to a file. That doesn't appear to be the case. If the returned array is
		    // null or has 0 length, we assume the path is to a file. This means empty
		    // directories will get turned into files.
		    if (contents == null || contents.length == 0)
		      throw new IOException();
		    
		    String root = Environment.getExternalStorageDirectory().toString();
			File myDir = new File(root,path);
			myDir.mkdirs();
			//File file = new File(myDir, fname);
		    
//		    // Make the directory.
//		    File dir = new File(getExternalFilesDir(), path);
//		    _field.append("\n"+path);
//		    dir.mkdirs();

		    // Recurse on the contents.
		    for (String entry : contents) {
		      copyAsset(path + "/" + entry);
		    }
		  } catch (IOException e) {
		    copyFileAsset(path);
		  }
		}

		/**
		 * Copy the asset file specified by path to app's data directory. Assumes
		 * parent directories have already been created.
		 * 
		 * @param path
		 * Path to asset, relative to app's assets directory.
		 */
		private void copyFileAsset(String path) {
			String root = Environment.getExternalStorageDirectory().toString();
			File file = new File(root,path);
		  try {
		    InputStream in = getAssets().open(path);
		    OutputStream out = new FileOutputStream(file);
		    byte[] buffer = new byte[1024];
		    int read = in.read(buffer);
		    while (read != -1) {
		      out.write(buffer, 0, read);
		      read = in.read(buffer);
		    }
		    out.close();
		    in.close();
		  } catch (IOException e) {
		    //Log.i(e);
		  }
		}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "called onCreateOptionsMenu");
		mdefault = menu.add("Default");
		mfastest = menu.add("Faatest");
		mmed = menu.add("Medium");
		mslowest = menu.add("Slowest");
		return true;
	}

	private class Translateclass extends AsyncTask<Void, Void, Void>{

		String translatedText = null;
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			Translate.setClientId("indicviewid");
		    Translate.setClientSecret("KCDKx9EFiaj3t8Ofhz5v5gLFenCT3wj2wOhsh4S/2Us=");
			try {
				  translatedText = Translate.execute(gtext, Language.HINDI, Language.ENGLISH);
				  //translatedText = Translate.execute("Bonjour le monde", Language.FRENCH, Language.ENGLISH);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			super.onPreExecute();
			return null;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			_field.setText(translatedText);
			 try {
					appendfile(translatedText);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			super.onPostExecute(result);
		}
	}
	 /* 
	 * public void onCameraViewStarted(int width, int height) { mRgba = new
	 * Mat(height, width, CvType.CV_8UC4); mIntermediateMat = new Mat(height,
	 * width, CvType.CV_8UC4); mGray = new Mat(height, width, CvType.CV_8UC1); }
	 * 
	 * public void onCameraViewStopped() { mRgba.release(); mGray.release();
	 * mIntermediateMat.release(); }
	 * 
	 * public Mat onCameraFrame(CvCameraViewFrame inputFrame) { final int
	 * viewMode = mViewMode; switch (viewMode) { case VIEW_MODE_GRAY: // input
	 * frame has gray scale format Imgproc.cvtColor(inputFrame.gray(), mRgba,
	 * Imgproc.COLOR_GRAY2RGBA, 4); break; case VIEW_MODE_RGBA: // input frame
	 * has RBGA format mRgba = inputFrame.rgba(); break; case VIEW_MODE_CANNY:
	 * // input frame has gray scale format mRgba = inputFrame.rgba();
	 * Imgproc.Canny(inputFrame.gray(), mIntermediateMat, 80, 100);
	 * Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
	 * break; case VIEW_MODE_FEATURES: // input frame has RGBA format mRgba =
	 * inputFrame.rgba(); mGray = inputFrame.gray();
	 * FindFeatures(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr()); break;
	 * }
	 * 
	 * return mRgba; }
	 */

	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

		if (item == mdefault) {
			tesstype = TessBaseAPI.OEM_DEFAULT;
		} else if (item == mslowest) {
			tesstype = TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED;
		} else if (item == mmed) {
			tesstype = TessBaseAPI.OEM_CUBE_ONLY;
		} else if (item == mfastest) {
			tesstype = TessBaseAPI.OEM_TESSERACT_ONLY;
		}

		return true;
	}
	 public static Bitmap scaleDownBitmap(Bitmap photo, int newHeight, Context context) {

		 final float densityMultiplier = context.getResources().getDisplayMetrics().density;        

		 int h= (int) (newHeight*densityMultiplier);
		 int w= (int) (h * photo.getWidth()/((double) photo.getHeight()));

		 photo=Bitmap.createScaledBitmap(photo, w, h, true);

		 return photo;
		 }
	protected void onPhotoTaken() throws IOException {
		_taken = true;
		bitmap = BitmapFactory.decodeFile(_path);// , options);
		//bitmap = scaleDownBitmap(bitmap,100,this);
		crop();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 3) {
			Log.i("MakeMachine", "resultCode: " + resultCode);
			switch (resultCode) {
			case 0:
				Log.i("MakeMachine", "User cancelled");
				_field.setText("Cancelled by User");
				break;

			case -1:
				try {
					onPhotoTaken();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// crop();
				break;
			}
		} else if (requestCode == 4) {
			if (resultCode == RESULT_OK) {
				try {
					Bundle extras = data.getExtras();
					bitmap = extras.getParcelable("data");
					_display.setImageBitmap(bitmap);
					_field.clearComposingText();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					String errortext = "Please reduce  window size";
					 Toast toast = Toast.makeText(this, errortext, Toast.LENGTH_SHORT);
				     toast.show();
				}
				Globals g = Globals.getInstance();
				if(g.getData())
					contprocess();
			}
		}
	}

	public void crop() {
		
		try {
			Intent intent = new Intent(this, CropImage.class);
			intent.putExtra("image-path", _path);
			intent.putExtra("scale", true);
			intent.putExtra("aspectX", 0);
			intent.putExtra("aspectY", 0);
			intent.putExtra("return-data", true);
			startActivityForResult(intent, 4);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String errortext = "Please reduce  window size";
			 Toast toast = Toast.makeText(this, errortext, Toast.LENGTH_SHORT);
		     toast.show();
		}
	}

	protected void CorrectImage() throws IOException {
		/*
		 * ExifInterface exif = new ExifInterface(_path); int exifOrientation =
		 * exif .getAttributeInt(ExifInterface.TAG_ORIENTATION,
		 * ExifInterface.ORIENTATION_NORMAL); int rotate = 0; switch
		 * (exifOrientation) { case ExifInterface.ORIENTATION_ROTATE_90: rotate
		 * = 90; break; case ExifInterface.ORIENTATION_ROTATE_180: rotate = 180;
		 * break; case ExifInterface.ORIENTATION_ROTATE_270: rotate = 270;
		 * break; } if (rotate != 0) { int w = bitmap.getWidth(); int h =
		 * bitmap.getHeight(); // Setting pre rotate Matrix mtx = new Matrix();
		 * mtx.preRotate(rotate); // Rotating Bitmap & convert to ARGB_8888,
		 * required by tess bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h,
		 * mtx, false); }
		 */
		bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
		
//		String root = Environment.getExternalStorageDirectory().toString();
//		File myDir = new File(root);
//		myDir.mkdirs();
//		String fname = "testi.png";
//		File file = new File(myDir, fname);
//		Log.i(TAG, "" + file);
//		if (file.exists())
//			file.delete();
//		try {
//			FileOutputStream out = new FileOutputStream(file);
//			bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
//			out.flush();
//			out.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		//_field.setText(Environment.getExternalStorageDirectory().toString()+"/testi.png");
		//m = Highgui.imread(Environment.getExternalStorageDirectory().toString()+"/testi.jpg");
		//while(!OpenCVLoader.initDebug()) {}
		//Utils.bitmapToMat(bitmap,m);
		
		//Mat dd;
		//m = Highgui.imread(Environment.getExternalStorageDirectory().getAbsolutePath() +"testi.jpg");
		//m = new Mat(5,10,CvType.CV_8UC1,new Scalar(0));
		//Mat image;// = new Mat ( bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8U);
		 //image = Highgui.imread(file.getAbsolutePath()+);
		// Utils.bitmapToMat(bitmap, ImageMat);
		// FindFeatures(byteArray.getNativeObjAddr());
		//FindFeatures(byteArray);
		Mat m = new Mat();
		if (OpenCVLoader.initDebug()) {
			Utils.bitmapToMat(bitmap,m);
		}
		//Mat toret= new Mat();
		ArrayList <Mat>toret = new ArrayList<Mat>();
		Imgproc.cvtColor(m, m, Imgproc.COLOR_RGBA2GRAY, 4);
		int i;
		try {
			
			FindFeatures(m.getNativeObjAddr(),1,0,m.getNativeObjAddr());
			//Imgproc.cvtColor(m, m, Imgproc.COLOR_GRAY2RGBA, 4);
			bitmap = Bitmap.createBitmap(m.cols(), m.rows(), Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(m,bitmap);
			_display.setImageBitmap(bitmap);
			int ret=0;
			i = 0;
			while(ret!=-1)
			{
				Mat tm=new Mat();
				Utils.bitmapToMat(bitmap,tm);
				Imgproc.cvtColor(tm,tm, Imgproc.COLOR_RGBA2GRAY, 4);
				toret.add(tm);
				ret = FindFeatures(m.getNativeObjAddr(),0,i,toret.get(i).getNativeObjAddr());
				Mat tmpp = toret.get(i);
				images.add(tmpp);
				i++;
				//Utils.bitmapToMat(bitmap,toret);
				//Imgproc.cvtColor(toret, toret, Imgproc.COLOR_RGBA2GRAY, 4);
			};
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_field.setText("Preprocessing  Done");
		toret.clear();
	}

//	protected void useapi() throws IOException {
//		TessBaseAPI baseApi = new TessBaseAPI();
//		String path2 = Environment.getExternalStorageDirectory()
//				+ "/tesseract-ocr";
//		baseApi.init(path2, "hin", TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED);
//		baseApi.setImage(bitmap);
//		String recognizedText = baseApi.getUTF8Text();
//		_field.clearComposingText();
//		_field.setText(recognizedText);
//		// _field.append("\n");
//		// _field.append(translate(recognizedText));
//		baseApi.end();
//		String translatedText = null;
//		try {
//			translatedText = Translate.execute("Bonjour le monde", Language.FRENCH, Language.ENGLISH);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	    _field.append("\n"+translatedText);
//	}
	
	final class OcrUse extends AsyncTask<Void, Void, Void> {
		String recognizedText;
		OcrUse()
		{
			recognizedText="";
		}
		@Override
		protected Void doInBackground(Void... params) {
			TessBaseAPI baseApi = new TessBaseAPI();
			String path2 = Environment.getExternalStorageDirectory()
					+ "/tesseract-ocr";
			baseApi.init(path2, "hin", TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED);
			baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK);
			if(images.size()==0)
			{
			Mat tm=new Mat();
			Utils.bitmapToMat(bitmap,tm);
			Imgproc.cvtColor(tm,tm, Imgproc.COLOR_RGBA2GRAY, 4);
			images.add(tm);
			}
			for(int i=0;i<images.size();i++)
			{
				Bitmap tmp = Bitmap.createBitmap(images.get(i).cols(), images.get(i).rows(), Bitmap.Config.ARGB_8888);
				Utils.matToBitmap(images.get(i),tmp);
				baseApi.setImage(tmp);
				String currword="";
				currword = baseApi.getUTF8Text();
				recognizedText=recognizedText + (" "+currword);
			}
			// _field.append("\n");
			// _field.append(translate(recognizedText));
			baseApi.end();
//			try {
//				translatedText = Translate.execute(recognizedText, Language.HINDI, Language.ENGLISH);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			_field.clearComposingText();
			_field.setText("The found text is :\n");
			_field.append(recognizedText);
			 gtext = recognizedText;
			 images.clear();
			 try {
				appendfile(gtext);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 Globals g = Globals.getInstance();
			 if(g.getData())
			 {
				 Translateclass tc = new  Translateclass();
				 tc.execute();
			 }
		}
		
	}
	protected void appendfile(String txt) throws IOException
	{
	     File file = new File(Environment.getExternalStorageDirectory() + "/outputs.txt");        
	     if (!file.exists()) {
	             try {
	                 file.createNewFile();
	             } catch (IOException e) {
	                 e.printStackTrace();
	             }}
	     FileOutputStream f = new FileOutputStream(file,true);
         PrintWriter pw = new PrintWriter(f);
         pw.println(txt+"\n");
         pw.flush();
         pw.close();
         f.close();
	}
	public native int FindFeatures(long mataddrRgba,int init,int wordno,long toret);
	//public native void FindFeatures(byte []bytearray);
}
