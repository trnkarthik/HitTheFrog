package com.trnkarthik.hitthefrog2;

import java.io.IOException;
import java.io.InputStream;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
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

public class GameActivity extends SimpleBaseGameActivity {

	//constants
	//Camera Dimensions
	private static int CAMERA_WIDTH = 480;
	private static int CAMERA_HEIGHT = 800;

	//Textures
	private ITextureRegion mBackgroundTextureRegion;
	private ITextureRegion FrogTextureRegion;
	private ITextureRegion BallTextureRegion;

	//Other Variables

	int randFrogX=0,randFrogY=0;


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
					return getAssets().open("gfx/game_background.png");
				}
			});

			ITexture FrogTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("gfx/frog.png");
				}
			});

			ITexture BallTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("gfx/ball.png");
				}
			});


			// 2 - Load bitmap textures into VRAM
			backgroundTexture.load();
			FrogTexture.load();
			BallTexture.load();


			// 3 - Set up texture regions
			this.mBackgroundTextureRegion = TextureRegionFactory.extractFromTexture(backgroundTexture);
			this.FrogTextureRegion = TextureRegionFactory.extractFromTexture(FrogTexture);
			this.BallTextureRegion = TextureRegionFactory.extractFromTexture(BallTexture);


			// 4 - Create the stacks

		} catch (IOException e) {
			Debug.e(e);
		}
	}

	@Override
	protected Scene onCreateScene() {
		
		//Reading level number from intent
		
		Integer level = getIntent().getExtras().getInt("level");


		if(level == 1)
		{
			Intent tempint= new Intent(GameActivity.this, Level1.class);
			startActivity(tempint);
		}
		
		
		// 1 - Create new scene
		final Scene scene = new Scene();
		Sprite backgroundSprite = new Sprite(0, 0, this.mBackgroundTextureRegion, getVertexBufferObjectManager());

		setFrogXYValues();

		//gameToast(randFrogX +  "     " + randFrogY);

		

		final Sprite FrogSprite = new Sprite(randFrogX , randFrogY, this.FrogTextureRegion, getVertexBufferObjectManager());

		final Sprite ballSprite = new Sprite(CAMERA_WIDTH/2, (9*CAMERA_HEIGHT)/10, this.BallTextureRegion, getVertexBufferObjectManager()){

			public boolean onAreaTouched(TouchEvent pTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY)
			{
				this.setPosition(pTouchEvent.getX() - this.getWidth() / 2, pTouchEvent.getY() - this.getHeight() / 2);
				return true;	
			}
		};

		FrogSprite.setSize(100,100);
		ballSprite.setSize(35,35);


		scene.attachChild(backgroundSprite);
		scene.attachChild(FrogSprite);
		scene.attachChild(ballSprite);

		scene.registerTouchArea(ballSprite);

		//gameToast("testing");


		scene.registerUpdateHandler(new IUpdateHandler() {

			@Override
			public void reset() {  }

			@Override
			public void onUpdate(float pSecondsElapsed) {

				if(ballSprite.collidesWith(FrogSprite))
				{
					gameToast("collides");
				}

			}
		});

		return scene;
	}




	private void setFrogXYValues() {

		randFrogX =  (int)(Math.random()*(CAMERA_WIDTH-80));
		randFrogY = (int)(Math.random()*(CAMERA_HEIGHT/8));

	}

	public void gameToast(final String msg) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(GameActivity.this, msg, Toast.LENGTH_SHORT).show();
			}
		});
	}


	
	
	//testing methods for menu 
	
	
	
	
	
	
	
	
	
	
	

}