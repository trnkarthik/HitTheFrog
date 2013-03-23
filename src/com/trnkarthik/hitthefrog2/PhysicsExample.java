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
import org.andengine.input.touch.detector.SurfaceGestureDetector;
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
import android.view.MotionEvent;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;


public class PhysicsExample extends SimpleBaseGameActivity implements IAccelerationListener, IOnSceneTouchListener {


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

	private static final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);

	//Textures
	private ITextureRegion mBackgroundTextureRegion;
	private ITextureRegion FrogTextureRegion;
	//	private ITextureRegion BallTextureRegion;

	//Other Variables

	int randFrogX=0,randFrogY=0;

	int attempts=0,score=0; 


	// ===========================================================
	// Fields
	// ===========================================================

	private BitmapTextureAtlas mBitmapTextureAtlas;

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

		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}

	@Override
	public void onCreateResources() {

		//loading animated stuff

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 512, 256, TextureOptions.BILINEAR);
		this.mBoxFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "anim_ball.png", 0, 0, 8, 4); // 64x32
		this.mBitmapTextureAtlas.load();

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

			/*ITexture BallTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("gfx/ball.png");
				}
			});
			 */

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
					ballmovable = false;
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
		//final Body frogbody;

		//frogbody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, face, BodyType.KinematicBody, FIXTURE_DEF);
		//frogbody.setLinearVelocity(10, 10);
		//frogbody.setAngularVelocity(10);
		//frogbody.setTransform(randFrogX, randFrogY, 0);

		FrogSprite.setSize(100,100);

		this.mScene.attachChild(FrogSprite);
		//this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(FrogSprite, frogbody, true, true));




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

				if(flinged)
				{
					ballbody.setActive(true);
					float tempx;
					float tempy;
					float newangle = 0;

					if(angleFlinged < 0)
					{
						newangle = Math.abs(angleFlinged);
					}
					else if(angleFlinged > 0)
					{
						newangle = angleFlinged +270;
					}

					tempx = (float) (velocityx * Math.cos(ball.getX()));
					tempy = (float) (velocityy * Math.cos(ball.getY()));

					gameToast(tempx + "  "+tempy);
					ballbody.setLinearVelocity(new Vector2(tempx, tempy));
					//ball.setRotation(newangle);

					//ballbody.applyLinearImpulse(velocityx, velocityy, ball.getX(), ball.getY());
					
					//ballbody.applyTorque(velocityy);
					//ballbody.applyForce(velocityx, velocityy, ball.getX(), ball.getY());
					//ballbody.app
					
					//ballbody.setTransform(new Vector2(tempx,tempy),newangle);
					
					flinged = false;
				}


				/*	if(face.collidesWith(FrogSprite))
					{
						attempts++;
						score+=100;
						face.stopAnimation();
						mScene.setIgnoreUpdate(true);
						gameAlert(1);

					}
				 */
				/*if(ball.collidesWith(top_boundary) || ball.collidesWith(down_boundary) || ball.collidesWith(left_boundary)||ball.collidesWith(right_boundary))
				{
					attempts++;
					//face.stopAnimation();
					mScene.setIgnoreUpdate(true);
					gameAlert(2);
				}*/

				if(attempts==5)
				{
					attempts =0;
					gameAlert(3);
					score =0;

				}


			}
		});










		return this.mScene;
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
//		final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX()*15, pAccelerationData.getY()*15);
		//final Vector2 gravity = Vector2Pool.obtain(0, 0);
		//this.mPhysicsWorld.setGravity(gravity);
		//Vector2Pool.recycle(gravity);
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

	// ===========================================================
	// Methods
	// ===========================================================

	private void addFace(final float pX, final float pY) {





	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================


	//other methods

	private void setFrogXYValues() {

		randFrogX =  (int)(Math.random()*(CAMERA_WIDTH-80));
		randFrogY = (int)(Math.random()*(CAMERA_HEIGHT/8));

	}

	public void gameToast(final String msg) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(PhysicsExample.this, msg, Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		AlertDialog.Builder builder = new AlertDialog.Builder(PhysicsExample.this);

		int remattempts = (5-attempts);
		switch (id) {
		case 1:
			builder.setMessage("Won!!! "+" Your Score :"+score);
			builder.setIcon(R.drawable.ic_launcher);
			builder.setTitle("You still have "+remattempts+" more attempts");
			break;

		case 2:
			builder.setMessage("Crashed! "+" Your Score :"+score);
			builder.setIcon(R.drawable.ic_launcher);
			builder.setTitle("Try Again!You still have "+remattempts+" more attempts");

			break;

		case 3:
			builder.setMessage("Game Over");
			builder.setIcon(R.drawable.ic_launcher);
			builder.setTitle("Your Total Score is :"+ score);

			break;

		default:
			return null;
		}   

		builder.setPositiveButton("OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mScene.reset();
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

				//				ballbody.setLinearVelocity(-(float) (600 * (Math.cos(angle))),
				//						-(float) (600 * (Math.sin(angle))));
			}

			//gameToast((float) (600 * (Math.cos(angle)))+"      "+(float) (600 * (Math.sin(angle))) + "\n "+angleInDegree);
			//			velocityX = (float) (600 * (Math.cos(angle)));
			//			velocityY = (float) (600 * (Math.sin(angle)));
			angleFlinged = angleInDegree;
			velocityx = velocityX;
			velocityy = velocityY;
			//gameToast(angleFlinged +"" );
			return true;

		}	

	}





}

