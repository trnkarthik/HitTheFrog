package com.trnkarthik.hitthefrog2;

import java.io.IOException;
import java.io.InputStream;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.io.in.IInputStreamOpener;
import org.andengine.util.debug.Debug;

import android.content.Intent;
import android.widget.Toast;


public class Welcome extends SimpleBaseGameActivity {

	//constants

	private static int CAMERA_WIDTH = 480;
	private static int CAMERA_HEIGHT = 800;
	private ITextureRegion mBackgroundTextureRegion;
	private ITextureRegion welcomePageButtonStart;
	private ITextureRegion welcomePageButtonHighScores;
	private ITextureRegion welcomePageButtonExit;



	@Override
	public EngineOptions onCreateEngineOptions() {
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}

	@Override
	protected void onCreateResources() {
		try {
			// 1 - Set up bitmap textures
			ITexture backgroundTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("gfx/welcome_background.png");
				}
			});

			ITexture welcomePageButtonStart = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("gfx/welcome_button_start.png");
				}
			});
			
			ITexture welcomePageButtonHighScores = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("gfx/welcome_button_high_scores.png");
				}
			});
			
			ITexture welcomePageButtonExit = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("gfx/welcome_button_exit.png");
				}
			});


			// 2 - Load bitmap textures into VRAM
			backgroundTexture.load();
			welcomePageButtonStart.load();
			welcomePageButtonHighScores.load();
			welcomePageButtonExit.load();

			// 3 - Set up texture regions
			this.mBackgroundTextureRegion = TextureRegionFactory.extractFromTexture(backgroundTexture);
			this.welcomePageButtonStart = TextureRegionFactory.extractFromTexture(welcomePageButtonStart);
			this.welcomePageButtonHighScores = TextureRegionFactory.extractFromTexture(welcomePageButtonHighScores);
			this.welcomePageButtonExit = TextureRegionFactory.extractFromTexture(welcomePageButtonExit);

			// 4 - Create the stacks

		} catch (IOException e) {
			Debug.e(e);
		}		
	}

	@Override
	protected Scene onCreateScene() {
		// 1 - Create new scene
		final Scene scene = new Scene();
		Sprite backgroundSprite = new Sprite(0, 0, this.mBackgroundTextureRegion, getVertexBufferObjectManager());
		scene.attachChild(backgroundSprite);


		Sprite welcomeButtonStart = new Sprite(CAMERA_WIDTH/2-CAMERA_WIDTH/4, CAMERA_HEIGHT/2, this.welcomePageButtonStart, getVertexBufferObjectManager()){

			public boolean onAreaTouched(TouchEvent pTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY)
			{
				if(pTouchEvent.isActionDown())
				{
					Intent intent = new Intent(Welcome.this, myMenuActivity.class);
					startActivity(intent);
					//gameToast("down!");
				}
				return super.onAreaTouched(pTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);

				//this.setPosition(pTouchEvent.getX() - this.getWidth() / 2, pTouchEvent.getY() - this.getHeight() / 2);
				//return true;


			}

		};
		
		//gameToast(welcomeButtonStart.getHeight()+"");

		Sprite welcomeButtonHighScores = new Sprite(CAMERA_WIDTH/2-CAMERA_WIDTH/4, CAMERA_HEIGHT/2+115, this.welcomePageButtonHighScores, getVertexBufferObjectManager()){

			public boolean onAreaTouched(TouchEvent pTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY)
			{
				if(pTouchEvent.isActionDown())
				{
					Intent intent = new Intent(Welcome.this, DatabaseDemoActivity.class);
					startActivity(intent);
					//gameToast("down!");
				}
				return super.onAreaTouched(pTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);

				//this.setPosition(pTouchEvent.getX() - this.getWidth() / 2, pTouchEvent.getY() - this.getHeight() / 2);
				//return true;

			}

		};
		
		Sprite welcomeButtonExit = new Sprite(CAMERA_WIDTH/2-CAMERA_WIDTH/4, CAMERA_HEIGHT/2+230, this.welcomePageButtonExit, getVertexBufferObjectManager()){

			public boolean onAreaTouched(TouchEvent pTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY)
			{
				if(pTouchEvent.isActionDown())
				{
					finish();
				}
				return super.onAreaTouched(pTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);

				//this.setPosition(pTouchEvent.getX() - this.getWidth() / 2, pTouchEvent.getY() - this.getHeight() / 2);
				//return true;

			}

		};

		//setting button sizes
		welcomeButtonStart.setSize(250,100);
		welcomeButtonHighScores.setSize(250,100);
		welcomeButtonExit.setSize(250,100);
		
		//attaching buttons to screen
		scene.attachChild(welcomeButtonStart);
		scene.attachChild(welcomeButtonHighScores);
		scene.attachChild(welcomeButtonExit);

		//registering touch events for buttons
		scene.registerTouchArea(welcomeButtonStart);
		scene.registerTouchArea(welcomeButtonHighScores);
		scene.registerTouchArea(welcomeButtonExit);

		return scene;	
	}

	public void gameToast(final String msg) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(Welcome.this, msg, Toast.LENGTH_SHORT).show();
			}
		});
	}

}
