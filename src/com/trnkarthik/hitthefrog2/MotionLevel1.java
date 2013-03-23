package com.trnkarthik.hitthefrog2;

import static org.andengine.extension.physics.box2d.util.constants.PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;

import java.io.IOException;
import java.io.InputStream;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.io.in.IInputStreamOpener;
import org.andengine.util.color.Color;
import org.andengine.util.debug.Debug;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.hardware.SensorManager;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;


public class MotionLevel1 extends SimpleBaseGameActivity implements IAccelerationListener, IOnSceneTouchListener {

	private static DataManager dm ;

	private GestureDetector mGestureDetector;

	// ===========================================================
	// Constants
	// ===========================================================


	public Boolean ballmovable = false;
	public Boolean flinged = false;
	float angleFlinged=0;
	float velocityx=0;
	float velocityy=0;

	private static final int CAMERA_WIDTH = 480;
	private static final int CAMERA_HEIGHT = 800;

	private Camera mCamera;


	private static final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);

	//Textures
	private ITextureRegion mBackgroundTextureRegion;
	private ITextureRegion FrogTextureRegion;

	private ITextureRegion mPausedTextureRegion;
	private CameraScene mPauseScene;
	
	
	
	//Other Variables

	int randFrogX=0,randFrogY=0;

	int attempts=0,score=0; 


	// ===========================================================
	// Fields
	// ===========================================================

	private BitmapTextureAtlas mBitmapTextureAtlas;
	private BitmapTextureAtlas mBitmapTextureAtlas2;

	private TiledTextureRegion mBoxFaceTextureRegion;

	private Scene mScene;

	private PhysicsWorld mPhysicsWorld;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public EngineOptions onCreateEngineOptions() {

//		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
	}

	@Override
	public void onCreateResources() {

		//loading animated stuff

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 512, 256, TextureOptions.BILINEAR);
		this.mBoxFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "anim_ball.png", 0, 0, 8, 4); // 64x32
		
		this.mBitmapTextureAtlas2 = new BitmapTextureAtlas(this.getTextureManager(), 512, 256, TextureOptions.BILINEAR);
		this.mPausedTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas2, this, "paused.png", 0, 0);

		this.mBitmapTextureAtlas.load();
		this.mBitmapTextureAtlas2.load();

		//normal stuff
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


			// 2 - Load bitmap textures into VRAM
			backgroundTexture.load();
			FrogTexture.load();
			//	BallTexture.load();


			// 3 - Set up texture regions
			this.mBackgroundTextureRegion = TextureRegionFactory.extractFromTexture(backgroundTexture);
			this.FrogTextureRegion = TextureRegionFactory.extractFromTexture(FrogTexture);
			//this.BallTextureRegion = TextureRegionFactory.extractFromTexture(BallTexture);


			// 4 - Create the stacks

		} catch (IOException e) {
			Debug.e(e);
		}
	}

	@Override
	public Scene onCreateScene() {
			
		//setting up the database
	    dm = new DataManager(MotionLevel1.this);
		
		//Pausing a game 

		this.mPauseScene = new CameraScene(this.mCamera);
		/* Make the 'PAUSED'-label centered on the camera. */
		final float centerX = (CAMERA_WIDTH - this.mPausedTextureRegion.getWidth()) / 2;
		final float centerY = (CAMERA_HEIGHT - this.mPausedTextureRegion.getHeight()) / 2;
		final Sprite pausedSprite = new Sprite(centerX, centerY, this.mPausedTextureRegion, this.getVertexBufferObjectManager());
		this.mPauseScene.attachChild(pausedSprite);
		/* Makes the paused Game look through. */
		this.mPauseScene.setBackgroundEnabled(false);


		runOnUiThread(new Runnable() {
			@Override
			public void run(){
				//setupGestureDetaction();

				mGestureDetector = new GestureDetector(getApplicationContext(),new CustomFlingDetector());
			}
		});		


		this.mEngine.registerUpdateHandler(new FPSLogger());

		//creating and configuring scene
		this.mScene = new Scene();
		this.mScene.setOnSceneTouchListener(this);


		//setting background


		Sprite backgroundSprite = new Sprite(0, 0, this.mBackgroundTextureRegion, getVertexBufferObjectManager());
		this.mScene.attachChild(backgroundSprite);

		//creating new physics world
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);

		//setting boundaries

		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT + 15, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle roof = new Rectangle(0, -15, CAMERA_WIDTH, 0, vertexBufferObjectManager);
		final Rectangle left = new Rectangle(-17, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		final Rectangle right = new Rectangle(CAMERA_WIDTH + 15, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);


		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);

		ground.setColor(Color.RED);
		roof.setColor(Color.RED);
		left.setColor(Color.RED);
		right.setColor(Color.RED);

		//adding boundaries to scene

		this.mScene.attachChild(ground);
		this.mScene.attachChild(roof);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);

		// boundaries to detect touch

		final Rectangle down_boundary = new Rectangle(0,CAMERA_HEIGHT,CAMERA_HEIGHT, 20, this.getVertexBufferObjectManager());
		final Rectangle top_boundary = new Rectangle(0, 0, CAMERA_WIDTH, 0, vertexBufferObjectManager);
		final Rectangle left_boundary =  new Rectangle(0, 0, 0, CAMERA_HEIGHT, vertexBufferObjectManager);
		final Rectangle right_boundary =  new Rectangle(CAMERA_WIDTH, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);

		right_boundary.setVisible(false);

		this.mScene.attachChild(down_boundary);
		this.mScene.attachChild(top_boundary);
		this.mScene.attachChild(left_boundary);
		this.mScene.attachChild(right_boundary);

		// caeating,handling and attaching ball

		final AnimatedSprite ball;

		final Body ballbody;

		ball = new AnimatedSprite(CAMERA_WIDTH/2, CAMERA_HEIGHT/2, this.mBoxFaceTextureRegion, this.getVertexBufferObjectManager())
		{

			public boolean onAreaTouched(TouchEvent pTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY)
			{

				if(pTouchEvent.isActionDown())
				{
					ballmovable = true;
				}

				if(pTouchEvent.isActionDown())
				{
					//ballmovable = false;
				}

				this.setPosition(pTouchEvent.getX() - this.getWidth() / 2, pTouchEvent.getY() - this.getHeight() / 2);
				return true;	
			}			

		};
		ballbody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, ball, BodyType.DynamicBody, FIXTURE_DEF);

		ball.setSize(35, 35);
		ball.animate(5);
		ball.setPosition(10, 10);
		final float angle = ballbody.getAngle(); 
		final Vector2 v2 = Vector2Pool.obtain(225 / PIXEL_TO_METER_RATIO_DEFAULT, 715 / PIXEL_TO_METER_RATIO_DEFAULT);
		ballbody.setTransform(v2, angle);
		Vector2Pool.recycle(v2);
		ballbody.setActive(false);

		this.mScene.attachChild(ball);
		this.mScene.registerTouchArea(ball);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(ball, ballbody, true, true));


		//generating random frog position

		setFrogXYValues();

		//creatig,handling and attaching frog

		final Sprite FrogSprite = new Sprite(randFrogX , randFrogY, this.FrogTextureRegion, getVertexBufferObjectManager());
		final Body frogbody;

		frogbody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, FrogSprite, BodyType.StaticBody, FIXTURE_DEF);
		float frogangle = frogbody.getAngle(); 
		final Vector2 frogv = Vector2Pool.obtain(randFrogX , randFrogY );
		frogbody.setTransform(frogv, frogangle);
		Vector2Pool.recycle(frogv);
		
		FrogSprite.setSize(100,100);

		this.mScene.attachChild(FrogSprite);
//		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(FrogSprite, frogbody, true, true));




		this.mScene.registerUpdateHandler(this.mPhysicsWorld);



		mScene.setOnSceneTouchListener(new IOnSceneTouchListener() {
			public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
				return mGestureDetector.onTouchEvent(pSceneTouchEvent.getMotionEvent());
			}
		});



		this.mScene.registerUpdateHandler(new IUpdateHandler() {

			@Override
			public void reset() {  }

			@Override
			public void onUpdate(float pSecondsElapsed) {

				
				if(ballmovable)
				{
					ballbody.setActive(true);
				}
				
				
					if(ball.collidesWith(FrogSprite))
					{
						score++;
						if(score ==5)
						{
						gameToast("Your hit "+score + "times in "+attempts+" attempts "+ "\n"+
											"Your Total Score "+attempts);
				        
				        Note note = new Note();
				        note.setSubject("Level 1 Normal");
				        note.setText(attempts +"");
				        dm.saveNote(note);

						score = 0 ;
						attempts = 0;
						}
						if(attempts ==0)
						{
							gameAlert(4);
						}
						
						mScene.setIgnoreUpdate(true);
						gameAlert(1);

						
						//mScene.reset();
					}
				
				if(ball.collidesWith(top_boundary) || ball.collidesWith(down_boundary) || ball.collidesWith(left_boundary)||ball.collidesWith(right_boundary))
				{
					attempts++;
					mEngine.enableVibrator(getApplicationContext());
					mEngine.vibrate(1000);
					mScene.setIgnoreUpdate(true);
					gameAlert(2);
				}

				}
		});




		return this.mScene;
	}



	//pausing effect
	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
		if(pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
			if(this.mEngine.isRunning()) {
				this.mScene.setChildScene(this.mPauseScene, false, true, true);
				this.mEngine.stop();
			} else {
				this.mScene.clearChildScene();
				this.mEngine.start();
			}
			return true;
		} else {
			return super.onKeyDown(pKeyCode, pEvent);
		}
	}








	@Override
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {



		float mFingerEndX = pSceneTouchEvent.getX();
		float mFingerEndY = pSceneTouchEvent.getY();

		// gameToast("x :"+mFingerEndX + "   \n y:"+mFingerEndY);

		return false;
	}

	@Override
	public void onAccelerationAccuracyChanged(final AccelerationData pAccelerationData) {

	}

	@Override
	public void onAccelerationChanged(final AccelerationData pAccelerationData) {
		 final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX()*25, pAccelerationData.getY()*25);
		this.mPhysicsWorld.setGravity(gravity);
		Vector2Pool.recycle(gravity);
	}

	@Override
	public void onResumeGame() {
		super.onResumeGame();

		this.enableAccelerationSensor(this);
	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();

		this.disableAccelerationSensor();
	}


	//other methods

	private void setFrogXYValues() {

		randFrogX =  (int)(Math.random()*(CAMERA_WIDTH-80));
		randFrogY = (int)(Math.random()*(CAMERA_HEIGHT/8));

	}

	public void gameToast(final String msg) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(MotionLevel1.this, msg, Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		AlertDialog.Builder builder = new AlertDialog.Builder(MotionLevel1.this);

		int remattempts = (5-attempts);
		switch (id) {
		case 1:
			builder.setMessage("Won!!! ");
			builder.setIcon(R.drawable.ic_launcher);
			builder.setTitle("Congrats!");
			break;

		case 2:
			builder.setMessage("Crashed! ");
			builder.setIcon(R.drawable.ic_launcher);
			builder.setTitle("Try Again!");

			break;

		case 3:
			builder.setMessage("Game Over");
			builder.setIcon(R.drawable.ic_launcher);
			builder.setTitle("Play Again :"+ attempts);

			break;
			
		case 4:
			builder.setMessage("New Game !");
			builder.setIcon(R.drawable.ic_launcher);
			builder.setTitle("Try hitting the frog 5 times sin minimum possible attempts!");

			break;

		default:
			return null;
		}   

		builder.setPositiveButton("OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				onCreateGame();

			}
		});
		builder.setNegativeButton("Cancel", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		AlertDialog alert = builder.create();
		return alert;

	}

	public void gameAlert(final Integer msg) {
		
		//gameToast(score +"  ls");
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showDialog(msg);
			}
		});
	}


	public class CustomFlingDetector extends SimpleOnGestureListener {


		@Override
		public boolean onDoubleTap(MotionEvent e) {
			gameToast("doubletap");
			return super.onDoubleTap(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

			flinged =true;
			float c;
			float sx = 0, sy = 0;
			float x1 = e1.getX();
			float y1 = e1.getY();

			float x2 = e2.getX();
			float y2 = e2.getY();

			float slope = (y2 - y1) / (x2 - x1);
			float angle = (float) Math.atan(slope);
			float angleInDegree = (float) Math.toDegrees(angle);

			c = y1 - (slope * x1);

			if (x1 > x2 && y1 > y2) {
				sx = CAMERA_WIDTH;
				sy = (slope * sx) + c;

			}

			angleFlinged = angleInDegree;
			velocityx = velocityX;
			velocityy = velocityY;
			attempts++ ;		
			//gameToast(attempts +"" );
			//gameToast("s "+score);				
			
			return true;

		}	

	}





}

