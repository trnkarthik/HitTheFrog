package com.trnkarthik.hitthefrog2;

import java.util.ArrayList;
import java.util.List;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.input.touch.detector.ClickDetector.IClickDetectorListener;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * @author Knoll Florian
 * @email myfknoll@gmail.com
 *
 */
public class myMenuActivity extends SimpleBaseGameActivity implements IScrollDetectorListener, IOnSceneTouchListener, IClickDetectorListener {

	// ===========================================================
	// Constants
	// ===========================================================
	protected static int CAMERA_WIDTH = 480;
	protected static int CAMERA_HEIGHT = 800;

	protected static int FONT_SIZE = 24;
	protected static int PADDING = 100;

	protected static int MENUITEMS = 7;


	// ===========================================================
	// Fields
	// ===========================================================
	private Scene mScene;
	private Camera mCamera;

	private BitmapTextureAtlas mMenuTextureAtlas;        
	private ITextureRegion mMenuLeftTextureRegion;
	private ITextureRegion mMenuRightTextureRegion;

	private Sprite menuleft;
	private Sprite menuright;

	// Scrolling
	private SurfaceScrollDetector mScrollDetector;
	private ClickDetector mClickDetector;

	private float mMinX = 0;
	private float mMaxX = 0;
	private float mCurrentX = 0;
	private int iItemClicked = -1;

	private Rectangle scrollBar;        
	private List<TextureRegion> columns = new ArrayList<TextureRegion>();
	
	public ArrayList<Class<?>> levellinks = new ArrayList<Class<?>>();

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
	protected void onCreateResources() {
		// Paths
		FontFactory.setAssetBasePath("font/");
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");               

		//Images for the menu
		for (int i = 0; i < MENUITEMS; i++) {				
			BitmapTextureAtlas mMenuBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 300,256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			ITextureRegion mMenuTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mMenuBitmapTextureAtlas, this, "menu"+i+".png", 0, 0);

			this.mEngine.getTextureManager().loadTexture(mMenuBitmapTextureAtlas);
			columns.add((TextureRegion) mMenuTextureRegion);


		}
		//Textures for menu arrows
		this.mMenuTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 128,128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mMenuLeftTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mMenuTextureAtlas, this, "menu_left.png", 0, 0);
		this.mMenuRightTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mMenuTextureAtlas, this, "menu_right.png",64, 0);
		this.mEngine.getTextureManager().loadTexture(mMenuTextureAtlas); 

	}

	@Override
	public EngineOptions onCreateEngineOptions() {
		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new FillResolutionPolicy(), this.mCamera);
		engineOptions.getTouchOptions().setNeedsMultiTouch(true);

		return engineOptions;
	}

	@Override
	protected Scene onCreateScene() {
		
		//populating levellinks arraylist
		
		levellinks.add(Level1.class);
		//levellinks.add(Level2.class);
		//levellinks.add(Level3.class);
		levellinks.add(MotionLevel1.class);
		//levellinks.add(MotionLevel2.class);
	//	levellinks.add(MotionLevel3.class);
		
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();
		this.mScene.setBackground(new Background(0, 0, 0));

		this.mScrollDetector = new SurfaceScrollDetector(this);
		this.mClickDetector = new ClickDetector(this);

		this.mScene.setOnSceneTouchListener(this);
		this.mScene.setTouchAreaBindingOnActionDownEnabled(true);
		this.mScene.setTouchAreaBindingOnActionMoveEnabled(true);
		this.mScene.setOnSceneTouchListenerBindingOnActionDownEnabled(true);

		CreateMenuBoxes();

		return this.mScene;

	}

	@Override
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
		this.mClickDetector.onTouchEvent(pSceneTouchEvent);
		this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
		return true;
	}

	@Override
	public void onScroll(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {

		//Disable the menu arrows left and right (15px padding)
		if(mCamera.getXMin()<=15)
			menuleft.setVisible(false);
		else
			menuleft.setVisible(true);

		if(mCamera.getXMin()>mMaxX-15)
			menuright.setVisible(false);
		else
			menuright.setVisible(true);

		//Return if ends are reached
		if ( ((mCurrentX - pDistanceX) < mMinX)  ){                	
			return;
		}else if((mCurrentX - pDistanceX) > mMaxX){

			return;
		}

		//Center camera to the current point
		this.mCamera.offsetCenter(-pDistanceX,0 );
		mCurrentX -= pDistanceX;

		//Set the scrollbar with the camera
		float tempX =mCamera.getCenterX()-CAMERA_WIDTH/2;
		// add the % part to the position
		tempX+= (tempX/(mMaxX+CAMERA_WIDTH))*CAMERA_WIDTH;      
		//set the position
		scrollBar.setPosition(tempX, scrollBar.getY());

		//set the arrows for left and right
		menuright.setPosition(mCamera.getCenterX()+CAMERA_WIDTH/2-menuright.getWidth(),menuright.getY());
		menuleft.setPosition(mCamera.getCenterX()-CAMERA_WIDTH/2,menuleft.getY());

		//Because Camera can have negativ X values, so set to 0
		if(this.mCamera.getXMin()<0){
			this.mCamera.offsetCenter(0,0 );
			mCurrentX=0;
		}


	}

	@Override
	public void onClick(ClickDetector pClickDetector, int pPointerID, float pSceneX, float pSceneY) {
		loadLevel(iItemClicked);
	};

	// ===========================================================
	// Methods
	// ===========================================================

	private void CreateMenuBoxes() {

		int spriteX = PADDING;
		int spriteY = PADDING;

		//current item counter
		int iItem = 1;
		int level = 1;

		//gameToast(columns.size()+"");

		for (int x = 0; x < columns.size(); x++) {

			if(columns.size()%2!=0 && x==columns.size()-1)
			{
				//spriteX += 220 + PADDING;
				//spriteY = PADDING;
			}

			//On Touch, save the clicked item in case it's a click and not a scroll.
			final int itemToLoad = iItem;


			Sprite sprite = new Sprite(spriteX,spriteY,(ITextureRegion)columns.get(x), this.getVertexBufferObjectManager()){

				public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
					iItemClicked = itemToLoad;
					
					Intent FromLevelToGame = new Intent(myMenuActivity.this, levellinks.get(itemToLoad-1));
					FromLevelToGame.putExtra("level", itemToLoad);
					startActivity(FromLevelToGame);		
					return false;
				}        			 
			};        		 
			iItem++;

			this.mScene.attachChild(sprite);        		 
			this.mScene.registerTouchArea(sprite);        		 


			//determining the level
			if(level == 1 )
			{
				//set position for the next input(level 2).
				//spriteX = PADDING;
				spriteY += 20 + PADDING+sprite.getHeight();
				level++;
			}
			else if(level == 2 )
			{                                         
				spriteX += 20 + PADDING+sprite.getWidth();
				spriteY = PADDING;
				level=1;
			}


		}



		mMaxX = spriteX - CAMERA_WIDTH;

		//set the size of the scrollbar
		float scrollbarsize = CAMERA_WIDTH/((mMaxX+CAMERA_WIDTH)/CAMERA_WIDTH);
		scrollBar = new Rectangle(0,CAMERA_HEIGHT-20,scrollbarsize, 2, this.getVertexBufferObjectManager());
		scrollBar.setColor(1,1,1);
		scrollBar.setAlpha((float) 0.5);
		this.mScene.attachChild(scrollBar);

		//menuleft button

		menuleft = new Sprite(0,CAMERA_HEIGHT/2-mMenuLeftTextureRegion.getHeight()/2,mMenuLeftTextureRegion, this.getVertexBufferObjectManager()){

			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) 
			{
				if(pSceneTouchEvent.isActionDown() || pSceneTouchEvent.isActionMove())
				{
					scrollBar.setAlpha(1);
					onScroll(mScrollDetector, 1, 100, 50);
				}
				//gameToast("Touched left");

				if(pSceneTouchEvent.isActionUp())
				{
					scrollBar.setAlpha((float) 0.5);
				}				

				return true;
			}    
		};


		//menuright button

		menuright = new Sprite(CAMERA_WIDTH-mMenuRightTextureRegion.getWidth(),CAMERA_HEIGHT/2-mMenuRightTextureRegion.getHeight()/2,mMenuRightTextureRegion, this.getVertexBufferObjectManager()){

			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) 
			{
				if(pSceneTouchEvent.isActionDown() || pSceneTouchEvent.isActionMove())
				{
					scrollBar.setAlpha(1);
					onScroll(mScrollDetector, 1, -100, 50);
				}
				//gameToast("Touched right");

				if(pSceneTouchEvent.isActionUp())
				{
					scrollBar.setAlpha((float) 0.5);
				}

				return true;
			}   

		};


		// registering touch area for menuleft and menuright

		this.mScene.registerTouchArea(menuleft);
		this.mScene.registerTouchArea(menuright);

		//attaching menuleft and menuright

		this.mScene.attachChild(menuright);
		menuleft.setVisible(false);
		this.mScene.attachChild(menuleft);
	}




	//Here is where you call the item load.
	private void loadLevel(final int iLevel) {
		if (iLevel != -1) {
			//gameToast( "Load Item" + String.valueOf(iLevel));
		}
	}


	@Override
	public void onScrollStarted(ScrollDetector pScollDetector,
			int pPointerID, float pDistanceX, float pDistanceY) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onScrollFinished(ScrollDetector pScollDetector,
			int pPointerID, float pDistanceX, float pDistanceY) {
		// TODO Auto-generated method stub

	}


	//other methods

	public void gameToast(final String msg) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(myMenuActivity.this, msg, Toast.LENGTH_SHORT).show();
			}
		});
	}


}
