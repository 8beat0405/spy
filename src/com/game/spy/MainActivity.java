//written by Taishi Sako 2014/04/10

package com.game.spy;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.engine.camera.BoundCamera;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.SmoothCamera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.shape.IAreaShape;
import org.andengine.entity.shape.Shape;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.extension.tmx.TMXLayer;
import org.andengine.extension.tmx.TMXLoader;
import org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener;
import org.andengine.extension.tmx.TMXObject;
import org.andengine.extension.tmx.TMXObjectGroup;
import org.andengine.extension.tmx.TMXProperties;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.extension.tmx.TMXTileProperty;
import org.andengine.extension.tmx.TMXTiledMap;
import org.andengine.extension.tmx.util.exception.TMXLoadException;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.view.RenderSurfaceView;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.ui.activity.SimpleLayoutGameActivity;
import org.andengine.util.color.Color;
import org.andengine.util.debug.Debug;
import org.json.JSONException;
import org.json.JSONObject;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.google.gson.Gson;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.os.Build;

public class MainActivity extends SimpleLayoutGameActivity implements IOnSceneTouchListener {
	
	private Scene scene;
	private TimerHandler enemyHandler;
	private TimerHandler resetHandler;
	
	private Random ran;
	private Random enemyRan;
	private int enemyMoveSec;
	
	private boolean scopeModeOn;
	private boolean wallCollided;
	private boolean objectCollided;
	private boolean playerMoving;
	private boolean pathFinding;
	private boolean cameraMoving;
	private boolean scrolling;
	private boolean pointerShown;
	private boolean objectHighlighted;
	private boolean justObtained;
	private boolean enemyMove;
	private boolean objectTargeted;
	private boolean floorTargeted;
	private boolean goalAchieved;
	private boolean alertOpened;
	
	private AlertDialog alertDialog;
	
	private int globalId;
	private int collidedGlobalId=100;
	private int targetedObjectGlobalId;
	
	private View view;
	
	private ArrayList<Integer> keyPosition;//マップ上の23個のオブジェクトとプレーヤーが「鍵」を有しているか判定する為のもの。要素が「0」の時は「入っていない」、「1」は「入っている」。最後の要素はプレーヤー用。つまりサイズはオブジェクト数+プレーヤー数。
	private Button keyButton;
	private ArrayList<Integer> suitcasePosition;//上記同様「スーツケース」判定用
	private Button suitcaseButton;
	private ArrayList<Integer> documentPosition;//上記同様「書類」判定用
	private Button documentButton;
	
	//配置されたオブジェクトのx,y
	private ArrayList<Integer> objX;
	private ArrayList<Integer> objY;
	
	private Rectangle highlight;
	private Rectangle objSensor;
	
	private float currentCenterX;
	private float currentCenterY;
	
	private float nextCenterX;
	private float nextCenterY;
	
	private float currentRectX;
	private float currentRectY;
	
	private float pressedX;
	private float pressedY;
	
	private float releasedX;
	private float releasedY;
	
	private float diffX;
	private float diffY;
	
	private float bodyDiffX;
	private float bodyDiffY;
	
	private float enemyDestinationX;
	private float enemyDestinationY;
	
	private float currentEnemyRectX;
	private float currentEnemyRectY;
	
	private float enemyBodyDiffX;
	private float enemyBodyDiffY;

	static final int CAMERA_WIDTH=512;
	static final int CAMERA_HEIGHT=288;
	private static int SPR_COLUMNS=3;
	private static int SPR_ROWS=4;

	private TMXTiledMap map;
	private TMXLayer bg;
	private TMXLayer lower;
	private TMXLayer upper;
	private TMXLayer objLayer;
	
	private ArrayList<Integer> objectTiles;
	
	private BitmapTextureAtlas texPlayer;
	private TiledTextureRegion regPlayer;
	private AnimatedSprite sprPlayer;
	
	private BitmapTextureAtlas texEnemy;
	private TiledTextureRegion regEnemy;
	private AnimatedSprite sprEnemy;
	
	private BitmapTextureAtlas texPointer;
	private TextureRegion regPointer;
	private Sprite sprPointer;
	
	private BitmapTextureAtlas texGoal;
	private TextureRegion regGoal;
	private Sprite sprGoal;
	
	private BitmapTextureAtlas texBall;
	private BitmapTextureAtlas texBall2;
	private TextureRegion regBall;
	private TextureRegion regBall2;
	
	private BitmapTextureAtlas texBelt;
	private TextureRegion regBelt;
	private Sprite sprBelt1;
	private Sprite sprBelt2;
	
	private BitmapTextureAtlas texKey;
	private TextureRegion regKey;
	private Sprite sprKey;
	
	private BitmapTextureAtlas texIconKey;
	private TextureRegion regIconKey;
	private Sprite sprIconKey;
	
	private BitmapTextureAtlas texIconSuitcase;
	private TextureRegion regIconSuitcase;
	private Sprite sprIconSuitcase;
	
	private BitmapTextureAtlas texIconDocument;
	private TextureRegion regIconDocument;
	private Sprite sprIconDocument;
	
	private BitmapTextureAtlas texTime;
	private TextureRegion regTime;
	private Sprite sprTime;
	
	private BitmapTextureAtlas texTrap;
	private TextureRegion regTrap;
	private Sprite sprTrap;
	
	private BitmapTextureAtlas texWeapon;
	private TextureRegion regWeapon;
	private Sprite sprWeapon;
	
	private BitmapTextureAtlas texBack;
	private TextureRegion regBack;
	private Sprite sprBack;
	
	private PhysicsWorld mPhysicsWorld;
	private Rectangle pRect;
	private Body pRectBody;
	
	private Rectangle eRect;
	private Body eRectBody;
	
	private Body targetBody;
	
	private Sprite sprBall;
	private Sprite sprMonsterBall;
	private Body pBallBody;
	private Body pBallBody2;
	
	private SmoothCamera camera;
	private HUD hud;
	public Music bgm;
	private SoundPool pool;
	static int confirm_id;
	static int toilet_id;
	static int door_id;
	static int book_id;
	static int metal_id;
	static int bingo_id;
	static int goal_id;
	static int reset_id;
	static int put_id;
	
	private int spawnX;
	private int spawnY;
	
	private int enemySpawnX;
	private int enemySpawnY;
	
	private FixtureDef fix;
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		// TODO Auto-generated method stub
		camera=new SmoothCamera(0,0,CAMERA_WIDTH,CAMERA_HEIGHT, 700.0f, 500.0f, 1.0f);
		
		currentCenterX=camera.getCenterX();
		currentCenterY=camera.getCenterY();
		
		EngineOptions eo=new EngineOptions(true,ScreenOrientation.LANDSCAPE_FIXED,new FillResolutionPolicy(),camera);
		eo.getAudioOptions().setNeedsMusic(true);
		eo.getAudioOptions().setNeedsSound(true);
		
		return eo;
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		// TODO Auto-generated method stub
//		if(goalAchieved=false){
		if(alertDialog !=null&&!alertDialog.isShowing()){
			alertOpened=false;
		}
		
		if(pSceneTouchEvent.isActionDown()){//画面が押された時の処理
			pressedX=pSceneTouchEvent.getX();//X座標を取得
			pressedY=pSceneTouchEvent.getY();//Y座標を取得
		}else if(pSceneTouchEvent.isActionUp()){//指が画面から離れた時の処理
			releasedX=pSceneTouchEvent.getX();//X座標を取得
			releasedY=pSceneTouchEvent.getY();//Y座標を取得
			
			//以下スワイプ判定の処理
			diffX=pressedX-releasedX;//画面を押してから離すまでのX軸の移動量
			diffY=pressedY-releasedY;//画面を押してから離すまでのY軸の移動量
			
			if(diffX>70){//X軸の移動量が+70ピクセル以上だった場合
				if(scopeModeOn==false&&scrolling==false){
					currentCenterX=camera.getCenterX();
					currentCenterY=camera.getCenterY();
					scopeModeOn=true;
					hud.attachChild(sprBack);
					hud.registerTouchArea(sprBack);
				}
				
				//右隣にbgレイヤーのマップが存在する場合は、カメラを右にスクロール
				if(scopeModeOn==true&&cameraMoving==false&&!(bg.getTMXTileAt(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY())==null)){
					nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
					nextCenterY=camera.getCenterY();
					camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
					cameraMoving=true;
					playerMoving=false;
				}
				
			}else if(diffX<-70){//X軸の移動量が-70ピクセル以下だった場合
				if(scopeModeOn==false&&scrolling==false){
					currentCenterX=camera.getCenterX();
					currentCenterY=camera.getCenterY();
					scopeModeOn=true;
					hud.attachChild(sprBack);
					hud.registerTouchArea(sprBack);
				}
				
				//左隣にbgレイヤーのマップが存在する場合は、カメラを左にスクロール
				if(scopeModeOn==true&&cameraMoving==false&&!(bg.getTMXTileAt(camera.getCenterX()-CAMERA_WIDTH, camera.getCenterY())==null)){
					nextCenterX=camera.getCenterX()-CAMERA_WIDTH;
					nextCenterY=camera.getCenterY();
					camera.setCenter(camera.getCenterX()-CAMERA_WIDTH, camera.getCenterY());
					cameraMoving=true;
					playerMoving=false;
				}

			}else if(diffY>70){//Y軸の移動量が+70ピクセル以上だった場合
				if(scopeModeOn==false&&scrolling==false){
					currentCenterX=camera.getCenterX();
					currentCenterY=camera.getCenterY();
					scopeModeOn=true;
					hud.attachChild(sprBack);
					hud.registerTouchArea(sprBack);
				}
				
				//上にbgレイヤーのマップが存在する場合は、カメラを上にスクロール
				if(scopeModeOn==true&&cameraMoving==false&&!(bg.getTMXTileAt(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT)==null)){
					nextCenterX=camera.getCenterX();
					nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
					camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
					cameraMoving=true;
					playerMoving=false;
				}

			}else if(diffY<-70){//Y軸の移動量が-70ピクセル以下だった場合
				if(scopeModeOn==false&&scrolling==false){
					currentCenterX=camera.getCenterX();
					currentCenterY=camera.getCenterY();
					scopeModeOn=true;
					hud.attachChild(sprBack);
					hud.registerTouchArea(sprBack);
				}
				
				//下にbgレイヤーのマップが存在する場合は、カメラを下にスクロール
				if(scopeModeOn==true&&cameraMoving==false&&!(bg.getTMXTileAt(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT)==null)){
					nextCenterX=camera.getCenterX();
					nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
					camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
					cameraMoving=true;
					playerMoving=false;
				}
				
			}else{//スワイプではなく、画面タッチの場合はキャラを移動する
				if(scopeModeOn==false&&scrolling==false){
				bodyDiffX=releasedX-currentRectX*PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
				bodyDiffY=releasedY-currentRectY*PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
				
				float ratioX = (bodyDiffX / Math.abs(bodyDiffX)) * Math.min(1,Math.abs(bodyDiffX / bodyDiffY));
				float ratioY = (bodyDiffY / Math.abs(bodyDiffY)) * Math.min(1,Math.abs(bodyDiffY / bodyDiffX));
				
				pRectBody.setLinearVelocity(new Vector2((ratioX*120)/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,(ratioY*120)/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT));
				playerMoving=true;
//				justObtained=false;
				
				if(pointerShown==true){
					scene.detachChild(sprPointer);
					pointerShown=false;
				}
				
				if(objectHighlighted==true){
					scene.detachChild(highlight);
					objectHighlighted=false;
				}
				
				sprPointer=new Sprite(releasedX-16, releasedY-15, regPointer,getVertexBufferObjectManager());
				sprPointer.setZIndex(0);
				scene.attachChild(sprPointer);
				pointerShown=true;
				
//				TMXTile tile=bg.getTMXTileAt(releasedX, releasedY);
//				highlight=new Rectangle((tile.getTileColumn())*32,(tile.getTileRow())*32,32,32,getVertexBufferObjectManager());
//				highlight.setColor(Color.GREEN);
//				highlight.setZIndex(0);
//				scene.attachChild(highlight);
//				objectHighlighted=true;
//				floorTargeted=false;
				
				if(objectTargeted==true){
					objectTargeted=false;
					scene.detachChild(objSensor);
				}else{
					floorTargeted=false;
					scene.detachChild(highlight);
				}
				
				targetedObjectGlobalId=0;
				loopTarget:for(int i=0;i<objX.size();i++){
					
					if(releasedX>=objX.get(i)&&releasedX<=objX.get(i)+32&&releasedY>=objY.get(i)-20&&releasedY<=objY.get(i)+32){
						targetedObjectGlobalId=objectTiles.get(i);
					}
//					Log.d("targetedObjectGlobalId", ""+targetedObjectGlobalId);
					
					if((objX.get(i)<=releasedX)&&(objX.get(i)+32>=releasedX)&&(objY.get(i)-20<=releasedY)&&(objY.get(i)+42>=releasedY)){
						objectTargeted=true;
//						targetedObjectGlobalId=i;
						
//                        objSensor = new Rectangle(objX.get(i)-10, objY.get(i)-10,50,50,getVertexBufferObjectManager());
                        objSensor = new Rectangle(objX.get(i), objY.get(i)-20,32,52,getVertexBufferObjectManager());
                        fix=PhysicsFactory.createFixtureDef(0f, 0f, 0f,true);
                        targetBody=PhysicsFactory.createBoxBody(this.mPhysicsWorld, objSensor, BodyType.DynamicBody, fix);
                        targetBody.setUserData("targetObj");
                        mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(objSensor,targetBody));
                        objSensor.setVisible(true);
                        objSensor.setColor(Color.PINK);
                        objSensor.setZIndex(100);
                        scene.attachChild(objSensor);
                        
						break loopTarget;
					}
				}
				
				Log.d("targetedObjectGlobalId", ""+targetedObjectGlobalId);
				
				if(objectTargeted==true){
					floorTargeted=false;
				}else{
					floorTargeted=true;
					objectHighlighted=true;
					TMXTile tile=bg.getTMXTileAt(releasedX, releasedY);
					highlight=new Rectangle((tile.getTileColumn())*32,(tile.getTileRow())*32,32,32,getVertexBufferObjectManager());
					highlight.setColor(Color.GREEN);
					highlight.setZIndex(0);
					scene.attachChild(highlight);
				}
				
				Log.d("touched", "objectTargeted:"+objectTargeted+" floorTargeted:"+floorTargeted);
				
				scene.sortChildren();
				
				if(Math.abs(bodyDiffX)>Math.abs(bodyDiffY)){
					if(bodyDiffX>0){
						sprPlayer.animate(new long[]{150,150,150},6,8,true);
					}else{
						sprPlayer.animate(new long[]{150,150,150},3,5,true);
					}
				}else{
					if(bodyDiffY>0){
						sprPlayer.animate(new long[]{150,150,150},0,2,true);
					}else{
						sprPlayer.animate(new long[]{150,150,150},9,11,true);
					}
				}
				
				}
			}
			
		}else if(pSceneTouchEvent.isActionMove()){

//		}
}
		return false;
		
	}

	@Override
	protected void onCreateResources() {
		// TODO Auto-generated method stub
		
		try{
			this.bgm=MusicFactory.createMusicFromAsset(this.mEngine.getMusicManager(), this, "mfx/bgm.ogg");
			this.bgm.setLooping(true);
			this.bgm.setVolume(0.5f);
		}catch(final IOException e){
			Debug.e("Music Read Error",e);
		}
		
		objectTiles=new ArrayList<Integer>();
		
		pool=new SoundPool(20,AudioManager.STREAM_MUSIC,0);
		confirm_id=pool.load(getApplicationContext(), R.raw.se_maoudamashii_system40,1);
		toilet_id=pool.load(getApplicationContext(), R.raw.se_maoudamashii_toire,1);
		door_id=pool.load(getApplicationContext(), R.raw.se_maoudamashii_se_door05,1);
		bingo_id=pool.load(getApplicationContext(), R.raw.jingle_1up,1);
		metal_id=pool.load(getApplicationContext(), R.raw.se_maoudamashii_se_sound06,1);
		book_id=pool.load(getApplicationContext(), R.raw.se_maoudamashii_se_paper01,1);
		goal_id=pool.load(getApplicationContext(), R.raw.ji_023,1);
		reset_id=pool.load(getApplicationContext(), R.raw.se_recovery,1);
		put_id=pool.load(getApplicationContext(), R.raw.se_lockon_1,1);
		
		enemyRan=new Random();
		
		texPlayer=new BitmapTextureAtlas(this.getTextureManager(),256,256,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		regPlayer=BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(texPlayer, this.getAssets(), "gfx/cape.png", 0, 0,SPR_COLUMNS,SPR_ROWS);
		texPlayer.load();
		
		texEnemy=new BitmapTextureAtlas(this.getTextureManager(),256,256,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		regEnemy=BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(texEnemy, this.getAssets(), "gfx/enemy.png", 0, 0,SPR_COLUMNS,SPR_ROWS);
		texEnemy.load();
		
		texGoal=new BitmapTextureAtlas(this.getTextureManager(),512,512,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		regGoal=BitmapTextureAtlasTextureRegionFactory.createFromAsset(texGoal, this.getAssets(), "gfx/goal.png",0,0);
		texGoal.load();
		
		texBall=new BitmapTextureAtlas(this.getTextureManager(),128,128,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		texBall2=new BitmapTextureAtlas(this.getTextureManager(),128,128,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		regBall=BitmapTextureAtlasTextureRegionFactory.createFromAsset(texBall, this.getAssets(), "gfx/football.png", 0, 0);
		regBall2=BitmapTextureAtlasTextureRegionFactory.createFromAsset(texBall2, this.getAssets(), "gfx/monster_ball.png", 0, 0);
		texBall.load();
		texBall2.load();
		
		texPointer=new BitmapTextureAtlas(this.getTextureManager(),64,64,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		regPointer=BitmapTextureAtlasTextureRegionFactory.createFromAsset(texPointer, this.getAssets(), "gfx/pointer.png",0,0);
		texPointer.load();
		
		texBelt=new BitmapTextureAtlas(this.getTextureManager(),256,256,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		regBelt=BitmapTextureAtlasTextureRegionFactory.createFromAsset(texBelt, this.getAssets(), "gfx/belt.png",0,0);
		texBelt.load();
		
		texKey=new BitmapTextureAtlas(this.getTextureManager(),128,64,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		regKey=BitmapTextureAtlasTextureRegionFactory.createFromAsset(texKey, this.getAssets(), "gfx/key.png",0,0);
		texKey.load();
		
		texIconKey=new BitmapTextureAtlas(this.getTextureManager(),32,32,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		regIconKey=BitmapTextureAtlasTextureRegionFactory.createFromAsset(texIconKey, this.getAssets(), "gfx/icon_key.png",0,0);
		texIconKey.load();
		
		texIconSuitcase=new BitmapTextureAtlas(this.getTextureManager(),32,32,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		regIconSuitcase=BitmapTextureAtlasTextureRegionFactory.createFromAsset(texIconSuitcase, this.getAssets(), "gfx/icon_suitcase.png",0,0);
		texIconSuitcase.load();
		
		texIconDocument=new BitmapTextureAtlas(this.getTextureManager(),32,32,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		regIconDocument=BitmapTextureAtlasTextureRegionFactory.createFromAsset(texIconDocument, this.getAssets(), "gfx/icon_document.png",0,0);
		texIconDocument.load();
		
		texTime=new BitmapTextureAtlas(this.getTextureManager(),128,64,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		regTime=BitmapTextureAtlasTextureRegionFactory.createFromAsset(texTime, this.getAssets(), "gfx/timeleft.png",0,0);
		texTime.load();
		
		texWeapon=new BitmapTextureAtlas(this.getTextureManager(),128,64,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		regWeapon=BitmapTextureAtlasTextureRegionFactory.createFromAsset(texWeapon, this.getAssets(), "gfx/weapon.png",0,0);
		texWeapon.load();
		
		texTrap=new BitmapTextureAtlas(this.getTextureManager(),128,64,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		regTrap=BitmapTextureAtlasTextureRegionFactory.createFromAsset(texTrap, this.getAssets(), "gfx/trap.png",0,0);
		texTrap.load();
		
		texBack=new BitmapTextureAtlas(this.getTextureManager(),64,64,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		regBack=BitmapTextureAtlasTextureRegionFactory.createFromAsset(texBack, this.getAssets(), "gfx/back.png",0,0);
		texBack.load();
		
	}

	@Override
	protected Scene onCreateScene() {
		// TODO Auto-generated method stub
		scene=new Scene();
		scene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));
		scene.setTouchAreaBindingOnActionDownEnabled(true);
		
		objX=new ArrayList<Integer>();
		objY=new ArrayList<Integer>();
		
    	keyPosition=new ArrayList<Integer>();
    	suitcasePosition=new ArrayList<Integer>();
    	documentPosition=new ArrayList<Integer>();
		
//		this.mEngine.registerUpdateHandler(new FPSLogger());
		
		if(!this.bgm.isPlaying()){
			this.bgm.play();
		}
		
		mPhysicsWorld=new PhysicsWorld(new Vector2(0,0),true,8,1);
		scene.registerUpdateHandler(mPhysicsWorld);
		
		//tmxファイルを読み込み、map変数に格納
	     try{
	         final TMXLoader tmxLoader = new TMXLoader(this.getAssets(), this.mEngine.getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA, this.getVertexBufferObjectManager(), new ITMXTilePropertiesListener() {
	       @Override
	       public void onTMXTileWithPropertiesCreated(final TMXTiledMap pTMXTiledMap, final TMXLayer pTMXLayer, final TMXTile pTMXTile, final TMXProperties<TMXTileProperty> pTMXTileProperties) {

	       }
	      });
	           this.map = tmxLoader.loadFromAsset("tmx/pop3.tmx");//tiledでmapを編集した後は、必ずタイルセットのimage sourceをgfx内から読むようにエディタで編集する。
	        }catch (final TMXLoadException tmxle) {
	         Debug.e(tmxle);
	        }
	     
	     //pop3.tmxのbgレイヤーをsceneに貼る
	     bg=this.map.getTMXLayers().get(0);
	     scene.attachChild(bg);
	     bg.setZIndex(0);
	     
	   //pop3.tmxのlowerレイヤーをsceneに貼る
	     lower=this.map.getTMXLayers().get(1);
	     scene.attachChild(lower);
	     lower.setZIndex(1);
	     
	   //pop3.tmxのupperレイヤーをsceneに貼る
	     upper=this.map.getTMXLayers().get(2);
	     scene.attachChild(upper);
	     upper.setZIndex(2);
	     
	     this.createUnwalkableObjects(map);
	     this.createInteractiveObjects(map);
	     
	     //プレーヤー用に要素を1つ増やす
	     keyPosition.add(0);
	     suitcasePosition.add(0);
	     documentPosition.add(0);
	     
	     putKeyObjects();
	     
//ボールの作成
//			final FixtureDef pCircleDef=PhysicsFactory.createFixtureDef(0, 1, 1);
//			sprBall=new Sprite(100,100,regBall,this.getVertexBufferObjectManager());
//			pBallBody=PhysicsFactory.createCircleBody(mPhysicsWorld, sprBall, BodyType.DynamicBody, pCircleDef);
//			pBallBody.setUserData("football");
//			sprBall.setZIndex(1);
//			scene.attachChild(sprBall);
//			mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(sprBall,pBallBody));
//			
//			final FixtureDef pCircleDef2=PhysicsFactory.createFixtureDef(0, 1, 0);
//			sprMonsterBall=new Sprite(150,150,regBall2,this.getVertexBufferObjectManager());
//			pBallBody2=PhysicsFactory.createCircleBody(mPhysicsWorld, sprMonsterBall, BodyType.DynamicBody, pCircleDef2);
//			pBallBody2.setUserData("monster_ball");
//			sprMonsterBall.setZIndex(1);
//			scene.attachChild(sprMonsterBall);
//			mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(sprMonsterBall,pBallBody2));
	     
	     //spawn pointをオブジェクトレイヤーから取る
			for (final TMXObjectGroup group : this.map
					.getTMXObjectGroups()) {
				 if (group.getTMXObjectGroupProperties().containsTMXProperty(
						"spawn", "true")) {
					getSpawnPoint(group.getTMXObjects());
				}
			}
			
			//enemySpawn pointをオブジェクトレイヤーから取る
			for (final TMXObjectGroup group : this.map
					.getTMXObjectGroups()) {
				 if (group.getTMXObjectGroupProperties().containsTMXProperty(
						"enemy", "true")) {
					getEnemySpawnPoint(group.getTMXObjects());
				}
			}
			
			//playerとenemyの作成
			sprPlayer = new AnimatedSprite(-3, -28, regPlayer,
					this.getVertexBufferObjectManager()){
				@Override
				public boolean onAreaTouched(TouchEvent te,float X,float Y){
					if(te.isActionUp()){
					}
					return true;
				}
			};
			
			sprEnemy = new AnimatedSprite(-3, -28, regEnemy,
					this.getVertexBufferObjectManager()){
				@Override
				public boolean onAreaTouched(TouchEvent te,float X,float Y){
					if(te.isActionUp()){
					}
					return true;
				}
			};
			
			scene.registerTouchArea(sprPlayer);
			scene.setOnSceneTouchListener(this);
			
//			pRect=new Rectangle(spawnX,spawnY,28,18,this.getVertexBufferObjectManager());
//			pRect.setColor(Color.TRANSPARENT);
//			pRect.setVisible(true);
//			pRect.attachChild(sprPlayer);
//			scene.attachChild(pRect);
//			pRect.setZIndex(1);
//			final FixtureDef pRectDef=PhysicsFactory.createFixtureDef(0f, 0f, 0f);
//			pRectBody=PhysicsFactory.createBoxBody(mPhysicsWorld, pRect, BodyType.DynamicBody, pRectDef);
//			pRectBody.setFixedRotation(true);
//			pRectBody.setUserData("player");
//			mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(pRect,pRectBody));
//			sprPlayer.setZIndex(1);
			
			eRect=new Rectangle(enemySpawnX,enemySpawnY,28,18,this.getVertexBufferObjectManager());
			eRect.setColor(Color.TRANSPARENT);
			eRect.setVisible(true);
			eRect.attachChild(sprEnemy);
			scene.attachChild(eRect);
			eRect.setZIndex(1);
			final FixtureDef eRectDef=PhysicsFactory.createFixtureDef(0f, 0f, 0f);
			eRectBody=PhysicsFactory.createBoxBody(mPhysicsWorld, eRect, BodyType.DynamicBody, eRectDef);
			eRectBody.setFixedRotation(true);
			eRectBody.setUserData("enemy");
			mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(eRect,eRectBody));
			sprEnemy.setZIndex(1);
			
			pRect=new Rectangle(spawnX,spawnY,28,18,this.getVertexBufferObjectManager());
			pRect.setColor(Color.TRANSPARENT);
			pRect.setVisible(true);
			pRect.attachChild(sprPlayer);
			scene.attachChild(pRect);
			pRect.setZIndex(1);
			final FixtureDef pRectDef=PhysicsFactory.createFixtureDef(0f, 0f, 0f);
			pRectBody=PhysicsFactory.createBoxBody(mPhysicsWorld, pRect, BodyType.DynamicBody, pRectDef);
			pRectBody.setFixedRotation(true);
			pRectBody.setUserData("player");
			mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(pRect,pRectBody));
			sprPlayer.setZIndex(1);
			
			//sceneとは独立したHUDレイヤーの作成
			hud=new HUD();
			camera.setHUD(hud);
			
			//画面左上部の黒い帯
			sprBelt1=new Sprite(0,0,regBelt,getVertexBufferObjectManager());
			sprBelt1.setZIndex(10);
			
			//画面右上部の黒い帯
			sprBelt2=new Sprite(288,0,regBelt,getVertexBufferObjectManager());
			sprBelt2.setZIndex(10);
			hud.attachChild(sprBelt1);
			hud.attachChild(sprBelt2);
			
			//左上の残り時間
			sprTime=new Sprite(5,8,regTime,getVertexBufferObjectManager());
			sprTime.setZIndex(11);
			hud.attachChild(sprTime);
			
			//Keyという文字
			sprKey=new Sprite(90,10,regKey,getVertexBufferObjectManager());
			sprKey.setZIndex(11);
			hud.attachChild(sprKey);
			
			//「鍵」を入手した時のアイコン
			sprIconKey=new Sprite(160,7,regIconKey,getVertexBufferObjectManager());
			sprIconKey.setVisible(false);
			sprIconKey.setZIndex(11);
			hud.attachChild(sprIconKey);
			
			//「スーツケース」を入手した時のアイコン
			sprIconSuitcase=new Sprite(125,7,regIconSuitcase,getVertexBufferObjectManager());
			sprIconSuitcase.setVisible(false);
			sprIconSuitcase.setZIndex(11);
			hud.attachChild(sprIconSuitcase);
			
			//「書類」を入手した時のアイコン
			sprIconDocument=new Sprite(195,7,regIconDocument,getVertexBufferObjectManager());
			sprIconDocument.setVisible(false);
			sprIconDocument.setZIndex(11);
			hud.attachChild(sprIconDocument);
			
			//Wpnという文字
			sprWeapon=new Sprite(290,10,regWeapon,getVertexBufferObjectManager());
			sprWeapon.setZIndex(11);
			hud.attachChild(sprWeapon);
			
			//Trapという文字
			sprTrap=new Sprite(390,10,regTrap,getVertexBufferObjectManager());
			sprTrap.setZIndex(11);
			hud.attachChild(sprTrap);
			
			//GOALの文字
			sprGoal=new Sprite(CAMERA_WIDTH/2,CAMERA_HEIGHT/2,regGoal,getVertexBufferObjectManager());
			sprGoal.setPosition(sprGoal.getX()-sprGoal.getWidth()/2, sprGoal.getY()-sprGoal.getHeight()/2);
			sprGoal.setZIndex(100);
			sprGoal.setVisible(false);
			hud.attachChild(sprGoal);
			
			//スコープモード時の左下のBackボタン
			sprBack=new Sprite(10,220,regBack,getVertexBufferObjectManager()){
				@Override
				public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
							scopeModeOn=false;
							camera.setMaxVelocity(6300f, 4500f);
							camera.setCenter(currentCenterX, currentCenterY);
							hud.detachChild(sprBack);
					        return true;
				}

			};
			
			hud.setTouchAreaBindingOnActionDownEnabled(true);
			
			sprBack.setZIndex(11);
			scene.attachChild(hud);
			
			cameraMoving=false;
			scrolling=false;
		
		scene.sortChildren();
		
		//毎FPSチェックするアップデートハンドラー
		this.scene.registerUpdateHandler(new IUpdateHandler(){
			@Override
			public void onUpdate(float pSecondsElapsed){
				if(goalAchieved==true){
					pool.play(goal_id, 7.0F, 7.0F, 1, 0, 1.0F);
					sprGoal.setVisible(true);
					resetGame();
				}
				
//				Log.d("scrolling",""+scrolling);
//				Log.d("camera moving",""+cameraMoving);

				//Backボタンが押されたあと、カメラの動く速度を元に戻す
				if(currentCenterX==camera.getCenterX()&&currentCenterY==camera.getCenterY()){
					camera.setMaxVelocity(700f, 500f);
					hud.unregisterTouchArea(sprBack);
				}
				
				//スコープモード時、カメラがスクロールし終わったらスイッチをfalseにする
				if((camera.getCenterX()==nextCenterX)&&(camera.getCenterY()==nextCenterY)){
					cameraMoving=false;
					scrolling=false;
				}
				
				currentRectX=pRectBody.getPosition().x;
				currentRectY=pRectBody.getPosition().y;
				
				float velocityX=pRectBody.getLinearVelocity().x;
				float velocityY=pRectBody.getLinearVelocity().y;
				
				//スコープモードに入った時とプレーヤーキャラが目的地にたどり着いた時は動きとアニメを止める
				//プレーヤーの移動をストップし、アニメーションを止める
				if((scopeModeOn==true)||((velocityX>0&&currentRectX*PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT>=releasedX)||(velocityX<0&&currentRectX*PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT<=releasedX))&&((velocityY>0&&currentRectY*PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT>=releasedY)||(velocityY<0&&currentRectY*PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT<=releasedY))){
					sprPlayer.stopAnimation();
					pRectBody.setLinearVelocity(0, 0);
					playerMoving=false;
					pathFinding=false;
					scene.detachChild(sprPointer);
				}
				
				//スコープモードから元の画面にスワイプで戻した時の処理
				if(scopeModeOn==true&&camera.getCenterX()==currentCenterX&&camera.getCenterY()==currentCenterY){
					scopeModeOn=false;
					cameraMoving=false;
					hud.detachChild(sprBack);
					hud.unregisterTouchArea(sprBack);
				}
				
				//壁やオブジェクトにぶつかった時に目的地までpath findingする処理
				if(pathFinding==true&&cameraMoving==false&&scrolling==false&&playerMoving==true){
//				if(pathFinding==true&&scrolling==false){
//					if(wallCollided==false||objectCollided==false){
					float bodyDiffX=releasedX-currentRectX*PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
					float bodyDiffY=releasedY-currentRectY*PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
					
					float ratioX = (bodyDiffX / Math.abs(bodyDiffX)) * Math.min(1,Math.abs(bodyDiffX / bodyDiffY));
					float ratioY = (bodyDiffY / Math.abs(bodyDiffY)) * Math.min(1,Math.abs(bodyDiffY / bodyDiffX));
					
					pRectBody.setLinearVelocity(new Vector2((ratioX*120)/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,(ratioY*120)/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT));
					
//					}
				}
				
//				Log.d("objectTargeted",""+objectTargeted);
				
				//敵キャラをランダム間隔で動かす処理
				if(enemyMove==false){
					enemyMoveSec=enemyRan.nextInt(10)+2;
					moveEnemy();
					scene.registerUpdateHandler(enemyHandler);
					enemyMove=true;
				}
				
				//スクロール時の処理
				if(scrolling==true){
					
					if(pointerShown==true){
						scene.detachChild(sprPointer);
						pointerShown=false;
					}
					
					if(objectHighlighted==true){
						scene.detachChild(highlight);
						objectHighlighted=false;
					}
					
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Handler handler = new Handler();
						    handler.postDelayed(new Runnable() {

						        @Override
						        public void run() {
						            // your code
						        	pRectBody.setLinearVelocity(0, 0);
						        	sprPlayer.stopAnimation();
						        	scrolling=false;
						        	pathFinding=false;
						        }
						    }, 450);
						}
						
					});

				}
				
			}

			@Override
			public void reset() {
				// TODO Auto-generated method stub
				
			}
		});
		
		addBoundaries();
		this.mPhysicsWorld.setContactListener(createContactListener());
		return scene;
	}
	
	private void getSpawnPoint(ArrayList<TMXObject> objects) {
		for (final TMXObject object : objects) {
			spawnX = object.getX();
			spawnY = object.getY();
		}
	}
	
	private void getEnemySpawnPoint(ArrayList<TMXObject> objects) {
		for (final TMXObject object : objects) {
			enemySpawnX = object.getX();
			enemySpawnY = object.getY();
		}
	}
	
	@Override
	protected int getLayoutID() {
		// TODO Auto-generated method stub
		return R.layout.activity_main;
	}

	@Override
	protected int getRenderSurfaceViewID() {
		// TODO Auto-generated method stub
		return R.id.renderview;
	}
	
	//壁を作るメソッド
    private void createUnwalkableObjects(TMXTiledMap map){
    	final FixtureDef boxFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 0);
        // Loop through the object groups
         for(final TMXObjectGroup group: this.map.getTMXObjectGroups()) {
                 if(group.getTMXObjectGroupProperties().containsTMXProperty("wall", "true")){
                         // This is our "wall" layer. Create the boxes from it
                         for(final TMXObject object : group.getTMXObjects()) {
                                final Rectangle rect = new Rectangle(object.getX(), object.getY(),object.getWidth(), object.getHeight(),getVertexBufferObjectManager());
                                Body wallBody=PhysicsFactory.createBoxBody(this.mPhysicsWorld, rect, BodyType.StaticBody, boxFixtureDef);
                                wallBody.setUserData("wall");
                                mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(rect,wallBody));
                                rect.setVisible(false);
                                rect.setColor(Color.BLUE);
                                rect.setZIndex(100);
                                scene.attachChild(rect);
                         }
                 }
         }
}
    
    //タイルマップのオブジェクトにBodyを当て、globalIdを振っていく。
    //pop3には23個のオブジェクトがある。
    private void createInteractiveObjects(TMXTiledMap map){
    	final FixtureDef boxFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 0);
        // Loop through the object groups
         for(final TMXObjectGroup group: this.map.getTMXObjectGroups()) {
                 if(group.getTMXObjectGroupProperties().containsTMXProperty("object", "true")){
                         // This is our "wall" layer. Create the boxes from it
                         for(final TMXObject object : group.getTMXObjects()) {
                        	 
                                final Rectangle rect = new Rectangle(object.getX(), object.getY(),object.getWidth(), object.getHeight(),getVertexBufferObjectManager());
                                Body objBody=PhysicsFactory.createBoxBody(this.mPhysicsWorld, rect, BodyType.StaticBody, boxFixtureDef);
                                
                                if(object.getTMXObjectProperties().containsTMXProperty("drawer", "true")){
                                	JSONObject json=new JSONObject();
                                	try {
										json.put("globalId", globalId);
										json.put("object", "drawer");
										objectTiles.add(globalId);
										
										Log.d("globalId",""+globalId);
										objX.add(object.getX());
										Log.d("objX",""+object.getX());
										objY.add(object.getY());
										Log.d("objY",""+object.getY());
										
										globalId++;
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
                                	objBody.setUserData(json);
                                	
                                	keyPosition.add(0);
                                	suitcasePosition.add(0);
                                	documentPosition.add(0);
                                	
                                }else if(object.getTMXObjectProperties().containsTMXProperty("metal_rack", "true")){
                                	JSONObject json=new JSONObject();
                                	try {
										json.put("globalId", globalId);
										json.put("object", "metal_rack");
										objectTiles.add(globalId);
										
										Log.d("globalId",""+globalId);
										objX.add(object.getX());
										Log.d("objX",""+object.getX());
										objY.add(object.getY());
										Log.d("objY",""+object.getY());
										
										globalId++;
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
                                	objBody.setUserData(json);
                                	
                                	keyPosition.add(0);
                                	suitcasePosition.add(0);
                                	documentPosition.add(0);
                                	
                                }else if(object.getTMXObjectProperties().containsTMXProperty("white_lockbox", "true")){
                                	JSONObject json=new JSONObject();
                                	try {
										json.put("globalId", globalId);
										json.put("object", "white_lockbox");
										objectTiles.add(globalId);
										
										Log.d("globalId",""+globalId);
										objX.add(object.getX());
										Log.d("objX",""+object.getX());
										objY.add(object.getY());
										Log.d("objY",""+object.getY());
										
										globalId++;
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
                                	objBody.setUserData(json);
                                	
                                	keyPosition.add(0);
                                	suitcasePosition.add(0);
                                	documentPosition.add(0);
                                	
                                }else if(object.getTMXObjectProperties().containsTMXProperty("black_lockbox", "true")){
                                	JSONObject json=new JSONObject();
                                	try {
										json.put("globalId", globalId);
										json.put("object", "black_lockbox");
										objectTiles.add(globalId);
										
										Log.d("globalId",""+globalId);
										objX.add(object.getX());
										Log.d("objX",""+object.getX());
										objY.add(object.getY());
										Log.d("objY",""+object.getY());
										
										globalId++;
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
                                	objBody.setUserData(json);
                                	
                                	keyPosition.add(0);
                                	suitcasePosition.add(0);
                                	documentPosition.add(0);
                                	
                                }else if(object.getTMXObjectProperties().containsTMXProperty("glasscase", "true")){
                                	JSONObject json=new JSONObject();
                                	try {
										json.put("globalId", globalId);
										json.put("object", "glasscase");
										objectTiles.add(globalId);
										
										Log.d("globalId",""+globalId);
										objX.add(object.getX());
										Log.d("objX",""+object.getX());
										objY.add(object.getY());
										Log.d("objY",""+object.getY());
										
										globalId++;
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
                                	objBody.setUserData(json);
                                	
                                	keyPosition.add(0);
                                	suitcasePosition.add(0);
                                	documentPosition.add(0);
                                	
                                }else if(object.getTMXObjectProperties().containsTMXProperty("plant", "true")){
                                	JSONObject json=new JSONObject();
                                	try {
										json.put("globalId", globalId);
										json.put("object", "plant");
										objectTiles.add(globalId);
										
										Log.d("globalId",""+globalId);
										objX.add(object.getX());
										Log.d("objX",""+object.getX());
										objY.add(object.getY());
										Log.d("objY",""+object.getY());
										
										globalId++;
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
                                	objBody.setUserData(json);
                                	
                                	keyPosition.add(0);
                                	suitcasePosition.add(0);
                                	documentPosition.add(0);
                                }else if(object.getTMXObjectProperties().containsTMXProperty("goal", "true")){ 
                                	objBody.setUserData("goal");
                                }else if(object.getTMXObjectProperties().containsTMXProperty("reset", "true")){
                                	objBody.setUserData("reset");
                                }
                                
                                mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(rect,objBody));
                                rect.setVisible(false);
                                rect.setColor(Color.BLUE);
                                rect.setZIndex(2);
                                scene.attachChild(rect);
                         }
                 }
         }
}
    
    private void createPointerObject(TMXTiledMap map){
    	for(final TMXObjectGroup group: this.map.getTMXObjectGroups()) {
    	if(group.getTMXObjectGroupProperties().containsTMXProperty("pointer", "true")){
            for(final TMXObject object : group.getTMXObjects()) {
                final Rectangle rect = new Rectangle(object.getX(), object.getY(),object.getWidth(), object.getHeight(),getVertexBufferObjectManager());
                rect.setVisible(true);
                rect.setColor(Color.WHITE);
                rect.setAlpha(0.5f);
                rect.setZIndex(4);
                scene.attachChild(rect);
            }
        }
    	}
    }
    
    //鍵、スーツケース、書類を23個のオブジェクトのどれかに置くメソッド
    private void putKeyObjects(){
    	//まず3つのArrayListの要素に全て0を入れる。0=「入っていない」 1=「入ってる」
    	for(int i=0;i<keyPosition.size();i++){
    		keyPosition.set(i, 0);
    	}
    	
    	for(int i=0;i<suitcasePosition.size();i++){
    		suitcasePosition.set(i, 0);
    	}
    	
    	for(int i=0;i<documentPosition.size();i++){
    		documentPosition.set(i, 0);
    	}
    	
    	//0-22までのランダムの数字を作り、鍵の場所を決める。
    	ran=new Random();
    	int keyRandom=ran.nextInt(23);
    	keyPosition.set(keyRandom, 1);
    	
    	//スーツケースの場所を決める。
    	//鍵の場所と重なっている場合は、異なる数字が出るまで繰り返す。
    	int suitcaseRandom=ran.nextInt(23);
    	while(suitcaseRandom==keyRandom){
    		suitcaseRandom=ran.nextInt(23);
    	}
    	suitcasePosition.set(suitcaseRandom, 1);
    	
    	//書類の場所を決める
    	//鍵、スーツケースと重なった場合は異なる数字が出るまで繰り返す。
    	int documentRandom=ran.nextInt(23);
    	while(documentRandom==suitcaseRandom||documentRandom==keyRandom){
    		documentRandom=ran.nextInt(23);
    	}
    	documentPosition.set(documentRandom, 1);
    	
    	Log.d("keyPosition",""+keyPosition);
    	Log.d("suitcasePosition",""+suitcasePosition);
    	Log.d("documentPosition",""+documentPosition);
}
	
    //各部屋のスクロール用のシェイプを作るメソッド。
	private void addBoundaries(){
		final FixtureDef wallDef=PhysicsFactory.createFixtureDef(0, 0, 0,true);

		//1,1
//		final Shape up1_1=new Rectangle(224,0,64,15,this.getVertexBufferObjectManager());
//		up1_1.setZIndex(10);
//		up1_1.setColor(Color.TRANSPARENT);
//		Body upBody1=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up1_1, BodyType.StaticBody, wallDef);
//		upBody1.setUserData("up1_1");
//		scene.attachChild(up1_1);
		
		final Shape down1_1=new Rectangle(224,263,64,25,this.getVertexBufferObjectManager());
		down1_1.setZIndex(10);
		down1_1.setColor(Color.TRANSPARENT);
		Body downBody1=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down1_1, BodyType.StaticBody, wallDef);
		downBody1.setUserData("down1_1");
		scene.attachChild(down1_1);
		
		final Shape right1_1=new Rectangle(488,112,25,64,this.getVertexBufferObjectManager());
		right1_1.setZIndex(10);
		right1_1.setColor(Color.TRANSPARENT);
		Body rightBody1_1=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right1_1, BodyType.StaticBody, wallDef);
		rightBody1_1.setUserData("right1_1");
		scene.attachChild(right1_1);
		
//		final Shape left1_1=new Rectangle(0,112,25,64,this.getVertexBufferObjectManager());
//		left1_1.setZIndex(10);
//		left1_1.setColor(Color.TRANSPARENT);
//		Body leftBody1_1=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left1_1, BodyType.StaticBody, wallDef);
//		leftBody1_1.setUserData("left1_1");
//		scene.attachChild(left1_1);
		
		//1,2
		final Shape up1_2=new Rectangle(224,288,64,30,this.getVertexBufferObjectManager());
		up1_2.setZIndex(10);
		up1_2.setColor(Color.TRANSPARENT);
		Body upBody1_2=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up1_2, BodyType.StaticBody, wallDef);
		upBody1_2.setUserData("up1_2");
		scene.attachChild(up1_2);
		
//		final Shape down1_2=new Rectangle(224,551,64,25,this.getVertexBufferObjectManager());
//		down1_2.setZIndex(10);
//		down1_2.setColor(Color.TRANSPARENT);
//		Body downBody1_2=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down1_2, BodyType.StaticBody, wallDef);
//		downBody1_2.setUserData("down1_2");
//		scene.attachChild(down1_2);
		
		final Shape right1_2=new Rectangle(488,400,25,64,this.getVertexBufferObjectManager());
		right1_2.setZIndex(10);
		right1_2.setColor(Color.TRANSPARENT);
		Body rightBody1_2=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right1_2, BodyType.StaticBody, wallDef);
		rightBody1_2.setUserData("right1_2");
		scene.attachChild(right1_2);
		
//		final Shape left1_2=new Rectangle(0,400,25,64,this.getVertexBufferObjectManager());
//		left1_2.setZIndex(10);
//		left1_2.setColor(Color.TRANSPARENT);
//		Body leftBody1_2=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left1_2, BodyType.StaticBody, wallDef);
//		leftBody1_2.setUserData("left1_2");
//		scene.attachChild(left1_2);
		
		//1,3
//		final Shape up1_3=new Rectangle(224,576,64,30,this.getVertexBufferObjectManager());
//		up1_3.setZIndex(10);
//		up1_3.setColor(Color.TRANSPARENT);
//		Body upBody1_3=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up1_3, BodyType.StaticBody, wallDef);
//		upBody1_3.setUserData("up1_3");
//		scene.attachChild(up1_3);
//		
//		final Shape down1_3=new Rectangle(224,839,64,25,this.getVertexBufferObjectManager());
//		down1_3.setZIndex(10);
//		down1_3.setColor(Color.TRANSPARENT);
//		Body downBody1_3=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down1_3, BodyType.StaticBody, wallDef);
//		downBody1_3.setUserData("down1_3");
//		scene.attachChild(down1_3);
//		
//		final Shape right1_3=new Rectangle(488,688,25,64,this.getVertexBufferObjectManager());
//		right1_3.setZIndex(10);
//		right1_3.setColor(Color.TRANSPARENT);
//		Body rightBody1_3=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right1_3, BodyType.StaticBody, wallDef);
//		rightBody1_3.setUserData("right1_3");
//		scene.attachChild(right1_3);
//		
//		final Shape left1_3=new Rectangle(0,688,25,64,this.getVertexBufferObjectManager());
//		left1_3.setZIndex(10);
//		left1_3.setColor(Color.TRANSPARENT);
//		Body leftBody1_3=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left1_3, BodyType.StaticBody, wallDef);
//		leftBody1_3.setUserData("left1_3");
//		scene.attachChild(left1_3);
//		
//		//1,4
//		final Shape up1_4=new Rectangle(224,864,64,30,this.getVertexBufferObjectManager());
//		up1_4.setZIndex(10);
//		up1_4.setColor(Color.TRANSPARENT);
//		Body upBody1_4=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up1_4, BodyType.StaticBody, wallDef);
//		upBody1_4.setUserData("up1_4");
//		scene.attachChild(up1_4);
//		
//		final Shape down1_4=new Rectangle(224,1127,64,25,this.getVertexBufferObjectManager());
//		down1_4.setZIndex(10);
//		down1_4.setColor(Color.TRANSPARENT);
//		Body downBody1_4=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down1_4, BodyType.StaticBody, wallDef);
//		downBody1_4.setUserData("down1_4");
//		scene.attachChild(down1_4);
//		
//		final Shape right1_4=new Rectangle(488,976,25,64,this.getVertexBufferObjectManager());
//		right1_4.setZIndex(10);
//		right1_4.setColor(Color.TRANSPARENT);
//		Body rightBody1_4=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right1_4, BodyType.StaticBody, wallDef);
//		rightBody1_4.setUserData("right1_4");
//		scene.attachChild(right1_4);
//		
//		final Shape left1_4=new Rectangle(0,976,25,64,this.getVertexBufferObjectManager());
//		left1_4.setZIndex(10);
//		left1_4.setColor(Color.TRANSPARENT);
//		Body leftBody1_4=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left1_4, BodyType.StaticBody, wallDef);
//		leftBody1_4.setUserData("left1_4");
//		scene.attachChild(left1_4);
//		
//		//1,5
//		final Shape up1_5=new Rectangle(224,1152,64,30,this.getVertexBufferObjectManager());
//		up1_5.setZIndex(10);
//		up1_5.setColor(Color.TRANSPARENT);
//		Body upBody1_5=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up1_5, BodyType.StaticBody, wallDef);
//		upBody1_5.setUserData("up1_5");
//		scene.attachChild(up1_5);
//		
//		final Shape down1_5=new Rectangle(224,1415,64,25,this.getVertexBufferObjectManager());
//		down1_5.setZIndex(10);
//		down1_5.setColor(Color.TRANSPARENT);
//		Body downBody1_5=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down1_5, BodyType.StaticBody, wallDef);
//		downBody1_5.setUserData("down1_5");
//		scene.attachChild(down1_5);
//		
//		final Shape right1_5=new Rectangle(488,1264,25,64,this.getVertexBufferObjectManager());
//		right1_5.setZIndex(10);
//		right1_5.setColor(Color.TRANSPARENT);
//		Body rightBody1_5=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right1_5, BodyType.StaticBody, wallDef);
//		rightBody1_5.setUserData("right1_5");
//		scene.attachChild(right1_5);
//		
//		final Shape left1_5=new Rectangle(0,1264,25,64,this.getVertexBufferObjectManager());
//		left1_5.setZIndex(10);
//		left1_5.setColor(Color.TRANSPARENT);
//		Body leftBody1_5=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left1_5, BodyType.StaticBody, wallDef);
//		leftBody1_5.setUserData("left1_5");
//		scene.attachChild(left1_5);
		
		//2,1
//		final Shape up2_1=new Rectangle(736,0,64,15,this.getVertexBufferObjectManager());
//		up2_1.setZIndex(10);
//		up2_1.setColor(Color.TRANSPARENT);
//		Body upBody2_1=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up2_1, BodyType.StaticBody, wallDef);
//		upBody2_1.setUserData("up2_1");
//		scene.attachChild(up2_1);
		
		final Shape down2_1=new Rectangle(736,263,64,25,this.getVertexBufferObjectManager());
		down2_1.setZIndex(10);
		down2_1.setColor(Color.TRANSPARENT);
		Body downBody2_1=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down2_1, BodyType.StaticBody, wallDef);
		downBody2_1.setUserData("down2_1");
		scene.attachChild(down2_1);
		
//		final Shape right2_1=new Rectangle(1000,112,25,64,this.getVertexBufferObjectManager());
//		right2_1.setZIndex(10);
//		right2_1.setColor(Color.TRANSPARENT);
//		Body rightBody2_1=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right2_1, BodyType.StaticBody, wallDef);
//		rightBody2_1.setUserData("right2_1");
//		scene.attachChild(right2_1);
		
		final Shape left2_1=new Rectangle(512,112,25,64,this.getVertexBufferObjectManager());
		left2_1.setZIndex(10);
		left2_1.setColor(Color.TRANSPARENT);
		Body leftBody2_1=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left2_1, BodyType.StaticBody, wallDef);
		leftBody2_1.setUserData("left2_1");
		scene.attachChild(left2_1);
		
		//2,2
		final Shape up2_2=new Rectangle(736,288,64,30,this.getVertexBufferObjectManager());
		up2_2.setZIndex(10);
		up2_2.setColor(Color.TRANSPARENT);
		Body upBody2_2=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up2_2, BodyType.StaticBody, wallDef);
		upBody2_2.setUserData("up2_2");
		scene.attachChild(up2_2);
		
//		final Shape down2_2=new Rectangle(736,551,64,25,this.getVertexBufferObjectManager());
//		down2_2.setZIndex(10);
//		down2_2.setColor(Color.TRANSPARENT);
//		Body downBody2_2=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down2_2, BodyType.StaticBody, wallDef);
//		downBody2_2.setUserData("down2_2");
//		scene.attachChild(down2_2);
		
//		final Shape right2_2=new Rectangle(1000,400,25,64,this.getVertexBufferObjectManager());
//		right2_2.setZIndex(10);
//		right2_2.setColor(Color.TRANSPARENT);
//		Body rightBody2_2=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right2_2, BodyType.StaticBody, wallDef);
//		rightBody2_2.setUserData("right2_2");
//		scene.attachChild(right2_2);
		
		final Shape left2_2=new Rectangle(512,400,25,64,this.getVertexBufferObjectManager());
		left2_2.setZIndex(10);
		left2_2.setColor(Color.TRANSPARENT);
		Body leftBody2_2=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left2_2, BodyType.StaticBody, wallDef);
		leftBody2_2.setUserData("left2_2");
		scene.attachChild(left2_2);
		
		//2,3
//		final Shape up2_3=new Rectangle(736,576,64,30,this.getVertexBufferObjectManager());
//		up2_3.setZIndex(10);
//		up2_3.setColor(Color.TRANSPARENT);
//		Body upBody2_3=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up2_3, BodyType.StaticBody, wallDef);
//		upBody2_3.setUserData("up2_3");
//		scene.attachChild(up2_3);
//		
//		final Shape down2_3=new Rectangle(736,839,64,25,this.getVertexBufferObjectManager());
//		down2_3.setZIndex(10);
//		down2_3.setColor(Color.TRANSPARENT);
//		Body downBody2_3=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down2_3, BodyType.StaticBody, wallDef);
//		downBody2_3.setUserData("down2_3");
//		scene.attachChild(down2_3);
//		
//		final Shape right2_3=new Rectangle(1000,688,25,64,this.getVertexBufferObjectManager());
//		right2_3.setZIndex(10);
//		right2_3.setColor(Color.TRANSPARENT);
//		Body rightBody2_3=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right2_3, BodyType.StaticBody, wallDef);
//		rightBody2_3.setUserData("right2_3");
//		scene.attachChild(right2_3);
//		
//		final Shape left2_3=new Rectangle(512,688,25,64,this.getVertexBufferObjectManager());
//		left2_3.setZIndex(10);
//		left2_3.setColor(Color.TRANSPARENT);
//		Body leftBody2_3=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left2_3, BodyType.StaticBody, wallDef);
//		leftBody2_3.setUserData("left2_3");
//		scene.attachChild(left2_3);
//		
//		//2,4
//		final Shape up2_4=new Rectangle(736,864,64,30,this.getVertexBufferObjectManager());
//		up2_4.setZIndex(10);
//		up2_4.setColor(Color.TRANSPARENT);
//		Body upBody2_4=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up2_4, BodyType.StaticBody, wallDef);
//		upBody2_4.setUserData("up2_4");
//		scene.attachChild(up2_4);
//		
//		final Shape down2_4=new Rectangle(736,1127,64,25,this.getVertexBufferObjectManager());
//		down2_4.setZIndex(10);
//		down2_4.setColor(Color.TRANSPARENT);
//		Body downBody2_4=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down2_4, BodyType.StaticBody, wallDef);
//		downBody2_4.setUserData("down2_4");
//		scene.attachChild(down2_4);
//		
//		final Shape right2_4=new Rectangle(1000,976,25,64,this.getVertexBufferObjectManager());
//		right2_4.setZIndex(10);
//		right2_4.setColor(Color.TRANSPARENT);
//		Body rightBody2_4=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right2_4, BodyType.StaticBody, wallDef);
//		rightBody2_4.setUserData("right2_4");
//		scene.attachChild(right2_4);
//		
//		final Shape left2_4=new Rectangle(512,976,25,64,this.getVertexBufferObjectManager());
//		left2_4.setZIndex(10);
//		left2_4.setColor(Color.TRANSPARENT);
//		Body leftBody2_4=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left2_4, BodyType.StaticBody, wallDef);
//		leftBody2_4.setUserData("left2_4");
//		scene.attachChild(left2_4);
//		
//		//2,5
//		final Shape up2_5=new Rectangle(736,1152,64,30,this.getVertexBufferObjectManager());
//		up2_5.setZIndex(10);
//		up2_5.setColor(Color.TRANSPARENT);
//		Body upBody2_5=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up2_5, BodyType.StaticBody, wallDef);
//		upBody2_5.setUserData("up2_5");
//		scene.attachChild(up2_5);
//		
//		final Shape down2_5=new Rectangle(736,1415,64,25,this.getVertexBufferObjectManager());
//		down2_5.setZIndex(10);
//		down2_5.setColor(Color.TRANSPARENT);
//		Body downBody2_5=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down2_5, BodyType.StaticBody, wallDef);
//		downBody2_5.setUserData("down2_5");
//		scene.attachChild(down2_5);
//		
//		final Shape right2_5=new Rectangle(1000,1264,25,64,this.getVertexBufferObjectManager());
//		right2_5.setZIndex(10);
//		right2_5.setColor(Color.TRANSPARENT);
//		Body rightBody2_5=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right2_5, BodyType.StaticBody, wallDef);
//		rightBody2_5.setUserData("right2_5");
//		scene.attachChild(right2_5);
//		
//		final Shape left2_5=new Rectangle(512,1264,25,64,this.getVertexBufferObjectManager());
//		left2_5.setZIndex(10);
//		left2_5.setColor(Color.TRANSPARENT);
//		Body leftBody2_5=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left2_5, BodyType.StaticBody, wallDef);
//		leftBody2_5.setUserData("left2_5");
//		scene.attachChild(left2_5);
//		
//		//3,1
////		final Shape up3_1=new Rectangle(1248,0,64,15,this.getVertexBufferObjectManager());
////		up3_1.setZIndex(10);
////		up3_1.setColor(Color.TRANSPARENT);
////		Body upBody3_1=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up3_1, BodyType.StaticBody, wallDef);
////		upBody3_1.setUserData("up3_1");
////		scene.attachChild(up3_1);
//		
//		final Shape down3_1=new Rectangle(1248,263,64,25,this.getVertexBufferObjectManager());
//		down3_1.setZIndex(10);
//		down3_1.setColor(Color.TRANSPARENT);
//		Body downBody3_1=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down3_1, BodyType.StaticBody, wallDef);
//		downBody3_1.setUserData("down3_1");
//		scene.attachChild(down3_1);
//		
//		final Shape right3_1=new Rectangle(1512,112,25,64,this.getVertexBufferObjectManager());
//		right3_1.setZIndex(10);
//		right3_1.setColor(Color.TRANSPARENT);
//		Body rightBody3_1=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right3_1, BodyType.StaticBody, wallDef);
//		rightBody3_1.setUserData("right3_1");
//		scene.attachChild(right3_1);
//		
//		final Shape left3_1=new Rectangle(1024,112,25,64,this.getVertexBufferObjectManager());
//		left3_1.setZIndex(10);
//		left3_1.setColor(Color.TRANSPARENT);
//		Body leftBody3_1=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left3_1, BodyType.StaticBody, wallDef);
//		leftBody3_1.setUserData("left3_1");
//		scene.attachChild(left3_1);
//		
//		//3,2
//		final Shape up3_2=new Rectangle(1248,288,64,30,this.getVertexBufferObjectManager());
//		up3_2.setZIndex(10);
//		up3_2.setColor(Color.TRANSPARENT);
//		Body upBody3_2=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up3_2, BodyType.StaticBody, wallDef);
//		upBody3_2.setUserData("up3_2");
//		scene.attachChild(up3_2);
//		
//		final Shape down3_2=new Rectangle(1248,551,64,25,this.getVertexBufferObjectManager());
//		down3_2.setZIndex(10);
//		down3_2.setColor(Color.TRANSPARENT);
//		Body downBody3_2=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down3_2, BodyType.StaticBody, wallDef);
//		downBody3_2.setUserData("down3_2");
//		scene.attachChild(down3_2);
//		
//		final Shape right3_2=new Rectangle(1512,400,25,64,this.getVertexBufferObjectManager());
//		right3_2.setZIndex(10);
//		right3_2.setColor(Color.TRANSPARENT);
//		Body rightBody3_2=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right3_2, BodyType.StaticBody, wallDef);
//		rightBody3_2.setUserData("right3_2");
//		scene.attachChild(right3_2);
//		
//		final Shape left3_2=new Rectangle(1024,400,25,64,this.getVertexBufferObjectManager());
//		left3_2.setZIndex(10);
//		left3_2.setColor(Color.TRANSPARENT);
//		Body leftBody3_2=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left3_2, BodyType.StaticBody, wallDef);
//		leftBody3_2.setUserData("left3_2");
//		scene.attachChild(left3_2);
//		
//		//3,3
//		final Shape up3_3=new Rectangle(1248,576,64,30,this.getVertexBufferObjectManager());
//		up3_3.setZIndex(10);
//		up3_3.setColor(Color.TRANSPARENT);
//		Body upBody3_3=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up3_3, BodyType.StaticBody, wallDef);
//		upBody3_3.setUserData("up3_3");
//		scene.attachChild(up3_3);
//		
//		final Shape down3_3=new Rectangle(1248,839,64,25,this.getVertexBufferObjectManager());
//		down3_3.setZIndex(10);
//		down3_3.setColor(Color.TRANSPARENT);
//		Body downBody3_3=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down3_3, BodyType.StaticBody, wallDef);
//		downBody3_3.setUserData("down3_3");
//		scene.attachChild(down3_3);
//		
//		final Shape right3_3=new Rectangle(1512,688,25,64,this.getVertexBufferObjectManager());
//		right3_3.setZIndex(10);
//		right3_3.setColor(Color.TRANSPARENT);
//		Body rightBody3_3=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right3_3, BodyType.StaticBody, wallDef);
//		rightBody3_3.setUserData("right3_3");
//		scene.attachChild(right3_3);
//		
//		final Shape left3_3=new Rectangle(1024,688,25,64,this.getVertexBufferObjectManager());
//		left3_3.setZIndex(10);
//		left3_3.setColor(Color.TRANSPARENT);
//		Body leftBody3_3=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left3_3, BodyType.StaticBody, wallDef);
//		leftBody3_3.setUserData("left3_3");
//		scene.attachChild(left3_3);
//		
//		//3,4
//		final Shape up3_4=new Rectangle(1248,864,64,30,this.getVertexBufferObjectManager());
//		up3_4.setZIndex(10);
//		up3_4.setColor(Color.TRANSPARENT);
//		Body upBody3_4=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up3_4, BodyType.StaticBody, wallDef);
//		upBody3_4.setUserData("up3_4");
//		scene.attachChild(up3_4);
//		
//		final Shape down3_4=new Rectangle(1248,1127,64,25,this.getVertexBufferObjectManager());
//		down3_4.setZIndex(10);
//		down3_4.setColor(Color.TRANSPARENT);
//		Body downBody3_4=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down3_4, BodyType.StaticBody, wallDef);
//		downBody3_4.setUserData("down3_4");
//		scene.attachChild(down3_4);
//		
//		final Shape right3_4=new Rectangle(1512,976,25,64,this.getVertexBufferObjectManager());
//		right3_4.setZIndex(10);
//		right3_4.setColor(Color.TRANSPARENT);
//		Body rightBody3_4=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right3_4, BodyType.StaticBody, wallDef);
//		rightBody3_4.setUserData("right3_4");
//		scene.attachChild(right3_4);
//		
//		final Shape left3_4=new Rectangle(1024,976,25,64,this.getVertexBufferObjectManager());
//		left3_4.setZIndex(10);
//		left3_4.setColor(Color.TRANSPARENT);
//		Body leftBody3_4=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left3_4, BodyType.StaticBody, wallDef);
//		leftBody3_4.setUserData("left3_4");
//		scene.attachChild(left3_4);
//		
//		//3,5
//		final Shape up3_5=new Rectangle(1248,1152,64,30,this.getVertexBufferObjectManager());
//		up3_5.setZIndex(10);
//		up3_5.setColor(Color.TRANSPARENT);
//		Body upBody3_5=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up3_5, BodyType.StaticBody, wallDef);
//		upBody3_5.setUserData("up3_5");
//		scene.attachChild(up3_5);
//		
//		final Shape down3_5=new Rectangle(1248,1415,64,25,this.getVertexBufferObjectManager());
//		down3_5.setZIndex(10);
//		down3_5.setColor(Color.TRANSPARENT);
//		Body downBody3_5=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down3_5, BodyType.StaticBody, wallDef);
//		downBody3_5.setUserData("down3_5");
//		scene.attachChild(down3_5);
//		
//		final Shape right3_5=new Rectangle(1512,1264,25,64,this.getVertexBufferObjectManager());
//		right3_5.setZIndex(10);
//		right3_5.setColor(Color.TRANSPARENT);
//		Body rightBody3_5=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right3_5, BodyType.StaticBody, wallDef);
//		rightBody3_5.setUserData("right3_5");
//		scene.attachChild(right3_5);
//		
//		final Shape left3_5=new Rectangle(1024,1264,25,64,this.getVertexBufferObjectManager());
//		left3_5.setZIndex(10);
//		left3_5.setColor(Color.TRANSPARENT);
//		Body leftBody3_5=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left3_5, BodyType.StaticBody, wallDef);
//		leftBody3_5.setUserData("left3_5");
//		scene.attachChild(left3_5);
//		
//		//4,1
//		final Shape up4_1=new Rectangle(1760,0,64,15,this.getVertexBufferObjectManager());
//		up4_1.setZIndex(10);
//		up4_1.setColor(Color.TRANSPARENT);
//		Body upBody4_1=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up4_1, BodyType.StaticBody, wallDef);
//		upBody4_1.setUserData("up4_1");
//		scene.attachChild(up4_1);
//		
//		final Shape down4_1=new Rectangle(1760,263,64,25,this.getVertexBufferObjectManager());
//		down4_1.setZIndex(10);
//		down4_1.setColor(Color.TRANSPARENT);
//		Body downBody4_1=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down4_1, BodyType.StaticBody, wallDef);
//		downBody4_1.setUserData("down4_1");
//		scene.attachChild(down4_1);
//		
//		final Shape right4_1=new Rectangle(2024,112,25,64,this.getVertexBufferObjectManager());
//		right4_1.setZIndex(10);
//		right4_1.setColor(Color.TRANSPARENT);
//		Body rightBody4_1=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right4_1, BodyType.StaticBody, wallDef);
//		rightBody4_1.setUserData("right4_1");
//		scene.attachChild(right4_1);
//		
//		final Shape left4_1=new Rectangle(1536,112,25,64,this.getVertexBufferObjectManager());
//		left4_1.setZIndex(10);
//		left4_1.setColor(Color.TRANSPARENT);
//		Body leftBody4_1=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left4_1, BodyType.StaticBody, wallDef);
//		leftBody4_1.setUserData("left4_1");
//		scene.attachChild(left4_1);
//		
//		//4,2
//		final Shape up4_2=new Rectangle(1760,288,64,30,this.getVertexBufferObjectManager());
//		up4_2.setZIndex(10);
//		up4_2.setColor(Color.TRANSPARENT);
//		Body upBody4_2=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up4_2, BodyType.StaticBody, wallDef);
//		upBody4_2.setUserData("up4_2");
//		scene.attachChild(up4_2);
//		
//		final Shape down4_2=new Rectangle(1760,551,64,25,this.getVertexBufferObjectManager());
//		down4_2.setZIndex(10);
//		down4_2.setColor(Color.TRANSPARENT);
//		Body downBody4_2=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down4_2, BodyType.StaticBody, wallDef);
//		downBody4_2.setUserData("down4_2");
//		scene.attachChild(down4_2);
//		
//		final Shape right4_2=new Rectangle(2024,400,25,64,this.getVertexBufferObjectManager());
//		right4_2.setZIndex(10);
//		right4_2.setColor(Color.TRANSPARENT);
//		Body rightBody4_2=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right4_2, BodyType.StaticBody, wallDef);
//		rightBody4_2.setUserData("right4_2");
//		scene.attachChild(right4_2);
//		
//		final Shape left4_2=new Rectangle(1536,400,25,64,this.getVertexBufferObjectManager());
//		left4_2.setZIndex(10);
//		left4_2.setColor(Color.TRANSPARENT);
//		Body leftBody4_2=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left4_2, BodyType.StaticBody, wallDef);
//		leftBody4_2.setUserData("left4_2");
//		scene.attachChild(left4_2);
//		
//		//4,3
//		final Shape up4_3=new Rectangle(1760,576,64,30,this.getVertexBufferObjectManager());
//		up4_3.setZIndex(10);
//		up4_3.setColor(Color.TRANSPARENT);
//		Body upBody4_3=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up4_3, BodyType.StaticBody, wallDef);
//		upBody4_3.setUserData("up4_3");
//		scene.attachChild(up4_3);
//		
//		final Shape down4_3=new Rectangle(1760,839,64,25,this.getVertexBufferObjectManager());
//		down4_3.setZIndex(10);
//		down4_3.setColor(Color.TRANSPARENT);
//		Body downBody4_3=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down4_3, BodyType.StaticBody, wallDef);
//		downBody4_3.setUserData("down4_3");
//		scene.attachChild(down4_3);
//		
//		final Shape right4_3=new Rectangle(2024,688,25,64,this.getVertexBufferObjectManager());
//		right4_3.setZIndex(10);
//		right4_3.setColor(Color.TRANSPARENT);
//		Body rightBody4_3=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right4_3, BodyType.StaticBody, wallDef);
//		rightBody4_3.setUserData("right4_3");
//		scene.attachChild(right4_3);
//		
//		final Shape left4_3=new Rectangle(1536,688,25,64,this.getVertexBufferObjectManager());
//		left4_3.setZIndex(10);
//		left4_3.setColor(Color.TRANSPARENT);
//		Body leftBody4_3=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left4_3, BodyType.StaticBody, wallDef);
//		leftBody4_3.setUserData("left4_3");
//		scene.attachChild(left4_3);
//		
//		//4,4
//		final Shape up4_4=new Rectangle(1760,864,64,30,this.getVertexBufferObjectManager());
//		up4_4.setZIndex(10);
//		up4_4.setColor(Color.TRANSPARENT);
//		Body upBody4_4=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up4_4, BodyType.StaticBody, wallDef);
//		upBody4_4.setUserData("up4_4");
//		scene.attachChild(up4_4);
//		
//		final Shape down4_4=new Rectangle(1760,1127,64,25,this.getVertexBufferObjectManager());
//		down4_4.setZIndex(10);
//		down4_4.setColor(Color.TRANSPARENT);
//		Body downBody4_4=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down4_4, BodyType.StaticBody, wallDef);
//		downBody4_4.setUserData("down4_4");
//		scene.attachChild(down4_4);
//		
//		final Shape right4_4=new Rectangle(2024,976,25,64,this.getVertexBufferObjectManager());
//		right4_4.setZIndex(10);
//		right4_4.setColor(Color.TRANSPARENT);
//		Body rightBody4_4=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right4_4, BodyType.StaticBody, wallDef);
//		rightBody4_4.setUserData("right4_4");
//		scene.attachChild(right4_4);
//		
//		final Shape left4_4=new Rectangle(1536,976,25,64,this.getVertexBufferObjectManager());
//		left4_4.setZIndex(10);
//		left4_4.setColor(Color.TRANSPARENT);
//		Body leftBody4_4=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left4_4, BodyType.StaticBody, wallDef);
//		leftBody4_4.setUserData("left4_4");
//		scene.attachChild(left4_4);
//		
//		//4,5
//		final Shape up4_5=new Rectangle(1760,1152,64,30,this.getVertexBufferObjectManager());
//		up4_5.setZIndex(10);
//		up4_5.setColor(Color.TRANSPARENT);
//		Body upBody4_5=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up4_5, BodyType.StaticBody, wallDef);
//		upBody4_5.setUserData("up4_5");
//		scene.attachChild(up4_5);
//		
//		final Shape down4_5=new Rectangle(1760,1415,64,25,this.getVertexBufferObjectManager());
//		down4_5.setZIndex(10);
//		down4_5.setColor(Color.TRANSPARENT);
//		Body downBody4_5=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down4_5, BodyType.StaticBody, wallDef);
//		downBody4_5.setUserData("down4_5");
//		scene.attachChild(down4_5);
//		
//		final Shape right4_5=new Rectangle(2024,1264,25,64,this.getVertexBufferObjectManager());
//		right4_5.setZIndex(10);
//		right4_5.setColor(Color.TRANSPARENT);
//		Body rightBody4_5=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right4_5, BodyType.StaticBody, wallDef);
//		rightBody4_5.setUserData("right4_5");
//		scene.attachChild(right4_5);
//		
//		final Shape left4_5=new Rectangle(1536,1264,25,64,this.getVertexBufferObjectManager());
//		left4_5.setZIndex(10);
//		left4_5.setColor(Color.TRANSPARENT);
//		Body leftBody4_5=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left4_5, BodyType.StaticBody, wallDef);
//		leftBody4_5.setUserData("left4_5");
//		scene.attachChild(left4_5);
//		
//		//5,1
//		final Shape up5_1=new Rectangle(2272,0,64,15,this.getVertexBufferObjectManager());
//		up5_1.setZIndex(10);
//		up5_1.setColor(Color.TRANSPARENT);
//		Body upBody5_1=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up5_1, BodyType.StaticBody, wallDef);
//		upBody5_1.setUserData("up5_1");
//		scene.attachChild(up5_1);
//		
//		final Shape down5_1=new Rectangle(2272,263,64,25,this.getVertexBufferObjectManager());
//		down5_1.setZIndex(10);
//		down5_1.setColor(Color.TRANSPARENT);
//		Body downBody5_1=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down5_1, BodyType.StaticBody, wallDef);
//		downBody5_1.setUserData("down5_1");
//		scene.attachChild(down5_1);
//		
//		final Shape right5_1=new Rectangle(2536,112,25,64,this.getVertexBufferObjectManager());
//		right5_1.setZIndex(10);
//		right5_1.setColor(Color.TRANSPARENT);
//		Body rightBody5_1=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right5_1, BodyType.StaticBody, wallDef);
//		rightBody5_1.setUserData("right5_1");
//		scene.attachChild(right5_1);
//		
//		final Shape left5_1=new Rectangle(2048,112,25,64,this.getVertexBufferObjectManager());
//		left5_1.setZIndex(10);
//		left5_1.setColor(Color.TRANSPARENT);
//		Body leftBody5_1=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left5_1, BodyType.StaticBody, wallDef);
//		leftBody5_1.setUserData("left5_1");
//		scene.attachChild(left5_1);
//		
//		//5,2
//		final Shape up5_2=new Rectangle(2272,288,64,30,this.getVertexBufferObjectManager());
//		up5_2.setZIndex(10);
//		up5_2.setColor(Color.TRANSPARENT);
//		Body upBody5_2=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up5_2, BodyType.StaticBody, wallDef);
//		upBody5_2.setUserData("up5_2");
//		scene.attachChild(up5_2);
//		
//		final Shape down5_2=new Rectangle(2272,551,64,25,this.getVertexBufferObjectManager());
//		down5_2.setZIndex(10);
//		down5_2.setColor(Color.TRANSPARENT);
//		Body downBody5_2=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down5_2, BodyType.StaticBody, wallDef);
//		downBody5_2.setUserData("down5_2");
//		scene.attachChild(down5_2);
//		
//		final Shape right5_2=new Rectangle(2536,400,25,64,this.getVertexBufferObjectManager());
//		right5_2.setZIndex(10);
//		right5_2.setColor(Color.TRANSPARENT);
//		Body rightBody5_2=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right5_2, BodyType.StaticBody, wallDef);
//		rightBody5_2.setUserData("right5_2");
//		scene.attachChild(right5_2);
//		
//		final Shape left5_2=new Rectangle(2048,400,25,64,this.getVertexBufferObjectManager());
//		left5_2.setZIndex(10);
//		left5_2.setColor(Color.TRANSPARENT);
//		Body leftBody5_2=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left5_2, BodyType.StaticBody, wallDef);
//		leftBody5_2.setUserData("left5_2");
//		scene.attachChild(left5_2);
//		
//		//5,3
//		final Shape up5_3=new Rectangle(2272,576,64,30,this.getVertexBufferObjectManager());
//		up5_3.setZIndex(10);
//		up5_3.setColor(Color.TRANSPARENT);
//		Body upBody5_3=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up5_3, BodyType.StaticBody, wallDef);
//		upBody5_3.setUserData("up5_3");
//		scene.attachChild(up5_3);
//		
//		final Shape down5_3=new Rectangle(2272,839,64,25,this.getVertexBufferObjectManager());
//		down5_3.setZIndex(10);
//		down5_3.setColor(Color.TRANSPARENT);
//		Body downBody5_3=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down5_3, BodyType.StaticBody, wallDef);
//		downBody5_3.setUserData("down5_3");
//		scene.attachChild(down5_3);
//		
//		final Shape right5_3=new Rectangle(2536,688,25,64,this.getVertexBufferObjectManager());
//		right5_3.setZIndex(10);
//		right5_3.setColor(Color.TRANSPARENT);
//		Body rightBody5_3=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right5_3, BodyType.StaticBody, wallDef);
//		rightBody5_3.setUserData("right5_3");
//		scene.attachChild(right5_3);
//		
//		final Shape left5_3=new Rectangle(2048,688,25,64,this.getVertexBufferObjectManager());
//		left5_3.setZIndex(10);
//		left5_3.setColor(Color.TRANSPARENT);
//		Body leftBody5_3=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left5_3, BodyType.StaticBody, wallDef);
//		leftBody5_3.setUserData("left5_3");
//		scene.attachChild(left5_3);
//		
//		//5,4
//		final Shape up5_4=new Rectangle(2272,864,64,30,this.getVertexBufferObjectManager());
//		up5_4.setZIndex(10);
//		up5_4.setColor(Color.TRANSPARENT);
//		Body upBody5_4=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up5_4, BodyType.StaticBody, wallDef);
//		upBody5_4.setUserData("up5_4");
//		scene.attachChild(up5_4);
//		
//		final Shape down5_4=new Rectangle(2272,1127,64,25,this.getVertexBufferObjectManager());
//		down5_4.setZIndex(10);
//		down5_4.setColor(Color.TRANSPARENT);
//		Body downBody5_4=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down5_4, BodyType.StaticBody, wallDef);
//		downBody5_4.setUserData("down5_4");
//		scene.attachChild(down5_4);
//		
//		final Shape right5_4=new Rectangle(2536,976,25,64,this.getVertexBufferObjectManager());
//		right5_4.setZIndex(10);
//		right5_4.setColor(Color.TRANSPARENT);
//		Body rightBody5_4=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right5_4, BodyType.StaticBody, wallDef);
//		rightBody5_4.setUserData("right5_4");
//		scene.attachChild(right5_4);
//		
//		final Shape left5_4=new Rectangle(2048,976,25,64,this.getVertexBufferObjectManager());
//		left5_4.setZIndex(10);
//		left5_4.setColor(Color.TRANSPARENT);
//		Body leftBody5_4=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left5_4, BodyType.StaticBody, wallDef);
//		leftBody5_4.setUserData("left5_4");
//		scene.attachChild(left5_4);
//		
//		//5,5
//		final Shape up5_5=new Rectangle(2272,1152,64,30,this.getVertexBufferObjectManager());
//		up5_5.setZIndex(10);
//		up5_5.setColor(Color.TRANSPARENT);
//		Body upBody5_5=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)up5_5, BodyType.StaticBody, wallDef);
//		upBody5_5.setUserData("up5_5");
//		scene.attachChild(up5_5);
//		
//		final Shape down5_5=new Rectangle(2272,1415,64,25,this.getVertexBufferObjectManager());
//		down5_5.setZIndex(10);
//		down5_5.setColor(Color.TRANSPARENT);
//		Body downBody5_5=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)down5_5, BodyType.StaticBody, wallDef);
//		downBody5_5.setUserData("down5_5");
//		scene.attachChild(down5_5);
//		
//		final Shape right5_5=new Rectangle(2536,1264,25,64,this.getVertexBufferObjectManager());
//		right5_5.setZIndex(10);
//		right5_5.setColor(Color.TRANSPARENT);
//		Body rightBody5_5=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)right5_5, BodyType.StaticBody, wallDef);
//		rightBody5_5.setUserData("right5_5");
//		scene.attachChild(right5_5);
//		
//		final Shape left5_5=new Rectangle(2048,1264,25,64,this.getVertexBufferObjectManager());
//		left5_5.setZIndex(10);
//		left5_5.setColor(Color.TRANSPARENT);
//		Body leftBody5_5=PhysicsFactory.createBoxBody(this.mPhysicsWorld, (IAreaShape)left5_5, BodyType.StaticBody, wallDef);
//		leftBody5_5.setUserData("left5_5");
//		scene.attachChild(left5_5);
	}
	
	//コリジョン時の処理
	private ContactListener createContactListener(){
		
		ContactListener cl=new ContactListener(){

			@Override
			public void beginContact(Contact contact) {
				// TODO Auto-generated method stub
				final Body x1=contact.getFixtureA().getBody();
				final Body x2=contact.getFixtureB().getBody();
				
				if((x1.getUserData()=="wall"&&x2.getUserData()=="player")||(x2.getUserData()=="wall"&&x1.getUserData()=="player")){
					wallCollided=true;
					pathFinding=true;
					if(pointerShown==true){
						scene.detachChild(sprPointer);
					}
				}
				
				if((x1.getUserData()=="enemy"&&x2.getUserData()=="player")||(x2.getUserData()=="enemy"&&x1.getUserData()=="player")){
					MainActivity.this.runOnUiThread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(getApplicationContext(), "enemy touched", Toast.LENGTH_SHORT).show();
						}
						
					});
				}
				
				//3つキーアイテムを所持してゴールに到達したらゴール
				if(keyPosition.get(23)==1&&suitcasePosition.get(23)==1&&documentPosition.get(23)==1){
					if((x1.getUserData()=="goal"&&x2.getUserData()=="player")||(x2.getUserData()=="goal"&&x1.getUserData()=="player")){
						goalAchieved=true;
						sprPlayer.stopAnimation();
						pRectBody.setLinearVelocity(0, 0);
//						pool.play(goal_id, 1.0F, 1.0F, 0, 0, 1.0F);
//						sprGoal.setVisible(true);

					}
				}
				
//				if(targetedObjectGlobalId==collidedGlobalId&&((x2.getUserData()=="targetObj"&&x1.getUserData()=="player")||(x1.getUserData()=="targetObj"&&x2.getUserData()=="player"))){
//					objectTargeted=false;
//					scene.detachChild(objSensor);
////					pRectBody.setLinearVelocity(0, 0);
////					sprPlayer.stopAnimation();
//					Log.d("sensor","ok");
//				}
				
				//アイテムを隠せるマップ上のオブジェクトに触った時の判定
				if((x2.getUserData()!="enemy"&&x1.getUserData()!="enemy")&&(x1.getUserData() =="player"||x2.getUserData() =="player")){
				Object obj=x1.getUserData();
				Object obj2=x2.getUserData();
				if(obj instanceof JSONObject){
					JSONObject temp=(JSONObject)obj;
					collidedGlobalId=temp.optInt("globalId");
					Log.d("collided globalId1",""+collidedGlobalId);
				}
				
				if(obj2 instanceof JSONObject){
					JSONObject temp=(JSONObject)obj2;
					collidedGlobalId=temp.optInt("globalId");
					Log.d("collided globalId2",""+collidedGlobalId);
				}
				}
				
				if(collidedGlobalId<23&&((x1.getUserData()=="player")||(x2.getUserData()=="player"))){
//					Log.d("collided globalId",""+collidedGlobalId);
					
					objectCollided=true;
					pathFinding=true;
					pRectBody.setLinearVelocity(0, 0);
					playerMoving=false;
					
					if(floorTargeted==false){
						sprPlayer.stopAnimation();
					}
					
					if(pointerShown==true){
						scene.detachChild(sprPointer);
						pointerShown=false;
					}
					
					//「鍵」をプレーヤーが所持していない場合
					//触れた物に鍵があるかチェック
					if(collidedGlobalId==targetedObjectGlobalId){
					if(floorTargeted==false&&keyPosition.get(23) !=1){
						for(int i=0;i<keyPosition.size();i++){
							if(keyPosition.get(i)==1&&collidedGlobalId==i){
								pool.play(bingo_id, 7.0F, 7.0F, 1, 0, 1.0F);
								keyPosition.set(i, 0);
								keyPosition.set(23, 1);
								sprIconKey.setVisible(true);
								justObtained=true;
								Log.d("keyObtained","true");
							}
						}
					}
					
					//「スーツケース」をプレーヤーが所持していない場合
					//触れた物にスーツケースがあるかチェック
					if(floorTargeted==false&&suitcasePosition.get(23) !=1){
						for(int i=0;i<suitcasePosition.size();i++){
							if(suitcasePosition.get(i)==1&&collidedGlobalId==i){
								pool.play(bingo_id, 7.0F, 7.0F, 1, 0, 1.0F);
								suitcasePosition.set(i, 0);
								suitcasePosition.set(23, 1);
								sprIconSuitcase.setVisible(true);
								justObtained=true;
								Log.d("suitcaseObtained","true");
							}
						}
					}
					
					//「書類」をプレーヤーが所持していない場合
					//触れた物に書類があるかチェック
					if(floorTargeted==false&&documentPosition.get(23) !=1){
						for(int i=0;i<documentPosition.size();i++){
							if(documentPosition.get(i)==1&&collidedGlobalId==i){
								pool.play(bingo_id, 7.0F, 7.0F, 1, 0, 1.0F);
								documentPosition.set(i, 0);
								documentPosition.set(23, 1);
								sprIconDocument.setVisible(true);
								justObtained=true;
								Log.d("documentObtained","true");
							}
						}
					}
					
					//アイテム所持時の「置く」処理
//					put:if(pRectBody.getLinearVelocity().x<=0&&pRectBody.getLinearVelocity().y<=0&&alertOpened==false&&objectTargeted==false&&floorTargeted==false&&justObtained==false&&(keyPosition.get(23)==1||suitcasePosition.get(23)==1||documentPosition.get(23)==1)){
						put:if(alertOpened==false&&objectTargeted==true&&floorTargeted==false&&justObtained==false&&(keyPosition.get(23)==1||suitcasePosition.get(23)==1||documentPosition.get(23)==1)){
						pRectBody.setLinearVelocity(0, 0);
						sprPlayer.stopAnimation();
						
						final AlertDialog.Builder alert=new AlertDialog.Builder(MainActivity.this);
						LayoutInflater inflater=LayoutInflater.from(MainActivity.this);
						view=inflater.inflate(R.layout.layout_keys, null);
						
						alert.setTitle("置くアイテムを選択して下さい");
						alert.setIcon(R.drawable.icon_put);

						keyButton=(Button)view.findViewById(R.id.button_key);
						keyButton.setSoundEffectsEnabled(false);
						suitcaseButton=(Button)view.findViewById(R.id.button_suitcase);
						suitcaseButton.setSoundEffectsEnabled(false);
						documentButton=(Button)view.findViewById(R.id.button_document);
						documentButton.setSoundEffectsEnabled(false);
						
						if(keyPosition.get(collidedGlobalId)==0&&suitcasePosition.get(collidedGlobalId)==0&&documentPosition.get(collidedGlobalId)==0){
						if(keyPosition.get(23)==1){
								keyButton.setVisibility(View.VISIBLE);
								keyButton.setOnClickListener(new View.OnClickListener() {
									
									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub
										pool.play(put_id, 7.0F, 7.0F, 1, 0, 1.0F);
										sprIconKey.setVisible(false);
										keyPosition.set(23, 0);
										keyPosition.set(collidedGlobalId, 1);
										alertDialog.dismiss();
										alertOpened=false;
									}
								});
						}
						
						if(suitcasePosition.get(23)==1){
								suitcaseButton.setVisibility(View.VISIBLE);
								suitcaseButton.setOnClickListener(new View.OnClickListener() {
									
									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub
										pool.play(put_id, 7.0F, 7.0F, 1, 0, 1.0F);
										sprIconSuitcase.setVisible(false);
										suitcasePosition.set(23, 0);
										suitcasePosition.set(collidedGlobalId, 1);
										alertDialog.dismiss();
										alertOpened=false;
									}
								});
						}
						
						if(documentPosition.get(23)==1){
								documentButton.setVisibility(View.VISIBLE);
								documentButton.setOnClickListener(new View.OnClickListener() {
									
									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub
										pool.play(put_id, 7.0F, 7.0F, 1, 0, 1.0F);
										sprIconDocument.setVisible(false);
										documentPosition.set(23, 0);
										documentPosition.set(collidedGlobalId, 1);
										alertDialog.dismiss();
										alertOpened=false;
									}
								});
						}
						}
						
						alert.setView(view);
						alert.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								alertOpened=false;
							}
						});
						
						MainActivity.this.runOnUiThread(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								if(alertOpened==false&&floorTargeted==false&&objectTargeted==true&&pRectBody.getLinearVelocity().x<=0&&pRectBody.getLinearVelocity().y<=0){
								alertDialog=alert.create();
								alertDialog.show();
								alertOpened=true;
								}
							}
							
						});
					}
				}
				}
				
				if(targetedObjectGlobalId==collidedGlobalId&&((x2.getUserData()=="targetObj"&&x1.getUserData()=="player")||(x1.getUserData()=="targetObj"&&x2.getUserData()=="player"))){
					objectTargeted=false;
					scene.detachChild(objSensor);
//					pRectBody.setLinearVelocity(0, 0);
//					sprPlayer.stopAnimation();
					Log.d("sensor","ok");
				}
				
				//スクロールの処理
				if((x1.getUserData()=="player"||x2.getUserData()=="player")&&goalAchieved==false&&wallCollided==false&&objectTargeted==false&&scrolling==false&&(x1.getUserData() != "enemy"&&x2.getUserData() != "enemy")){
//					if(!(collidedGlobalId<23)){
//						sprPlayer.stopAnimation();
//						Log.d("scroll","passed");
//						cameraMoving=true;
//						scrolling=true;
//					}
					
					if(x1.getUserData()=="down1_1"||x2.getUserData()=="down1_1"){
						sprPlayer.stopAnimation();
						Log.d("scroll down1-1","passed");
						cameraMoving=true;
						scrolling=true;
						
						collidedGlobalId=100;
						
						nextCenterX=camera.getCenterX();
						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
						pRectBody.setLinearVelocity(0, 6);
						sprPlayer.animate(new long[]{150,150,150},0,2,false);
					}else if(x1.getUserData()=="right1_1"||x2.getUserData()=="right1_1"){
						sprPlayer.stopAnimation();
						Log.d("scroll right1-1","passed");
						cameraMoving=true;
						scrolling=true;
						
						collidedGlobalId=100;
						
						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
						nextCenterY=camera.getCenterY();
						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
						pRectBody.setLinearVelocity(6, 0);
						sprPlayer.animate(new long[]{150,150,150},6,8,true);
					}else if(x1.getUserData()=="up1_2"||x2.getUserData()=="up1_2"){
						sprPlayer.stopAnimation();
						Log.d("scroll up1-2","passed");
						cameraMoving=true;
						scrolling=true;
						
						collidedGlobalId=100;
						
						nextCenterX=camera.getCenterX();
						nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
						camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
						pRectBody.setLinearVelocity(0, -6);
						sprPlayer.animate(new long[]{150,150,150},9,11,true);
					}else if(x1.getUserData()=="right1_2"||x2.getUserData()=="right1_2"){
						sprPlayer.stopAnimation();
						Log.d("scroll right1-2","passed");
						cameraMoving=true;
						scrolling=true;
						
						collidedGlobalId=100;
						
						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
						nextCenterY=camera.getCenterY();
						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
						pRectBody.setLinearVelocity(6, 0);
						sprPlayer.animate(new long[]{150,150,150},6,8,true);
					}
//					}else if(x2.getUserData()=="down1_2"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, 6);
//						sprPlayer.animate(new long[]{150,150,150},0,2,false);
//					}else if(x2.getUserData()=="up1_3"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, -6);
//						sprPlayer.animate(new long[]{150,150,150},9,11,true);
//					}else if(x2.getUserData()=="right1_3"){
//						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(6, 0);
//						sprPlayer.animate(new long[]{150,150,150},6,8,true);
//					}else if(x2.getUserData()=="down1_3"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, 6);
//						sprPlayer.animate(new long[]{150,150,150},0,2,false);
//					}else if(x2.getUserData()=="up1_4"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, -6);
//						sprPlayer.animate(new long[]{150,150,150},9,11,true);
//					}else if(x2.getUserData()=="right1_4"){
//						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(6, 0);
//						sprPlayer.animate(new long[]{150,150,150},6,8,true);
//					}else if(x2.getUserData()=="down1_4"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, 6);
//						sprPlayer.animate(new long[]{150,150,150},0,2,false);
//					}else if(x2.getUserData()=="up1_5"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, -6);
//						sprPlayer.animate(new long[]{150,150,150},9,11,true);
//					}else if(x2.getUserData()=="right1_5"){
//						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(6, 0);
//						sprPlayer.animate(new long[]{150,150,150},6,8,true);
					
					else if(x1.getUserData()=="left2_1"||x2.getUserData()=="left2_1"){
						sprPlayer.stopAnimation();
						Log.d("scroll left2-1","passed");
						cameraMoving=true;
						scrolling=true;
						
						collidedGlobalId=100;
						
						nextCenterX=camera.getCenterX()-CAMERA_WIDTH;
						nextCenterY=camera.getCenterY();
						camera.setCenter(camera.getCenterX()-CAMERA_WIDTH, camera.getCenterY());
						pRectBody.setLinearVelocity(-6, 0);
						sprPlayer.animate(new long[]{150,150,150},3,5,true);
//					}else if(x2.getUserData()=="right2_1"){
//						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(6, 0);
//						sprPlayer.animate(new long[]{150,150,150},6,8,true);
//					}
					}else if(x1.getUserData()=="down2_1"||x2.getUserData()=="down2_1"){
						sprPlayer.stopAnimation();
						Log.d("scroll down2-1","passed");
						cameraMoving=true;
						scrolling=true;
						
						collidedGlobalId=100;
						
						nextCenterX=camera.getCenterX();
						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
						pRectBody.setLinearVelocity(0, 6);
						sprPlayer.animate(new long[]{150,150,150},0,2,false);
					}else if(x1.getUserData()=="up2_2"||x2.getUserData()=="up2_2"){
						sprPlayer.stopAnimation();
						Log.d("scroll up2-2","passed");
						cameraMoving=true;
						scrolling=true;
						
						collidedGlobalId=100;
						
						nextCenterX=camera.getCenterX();
						nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
						camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
						pRectBody.setLinearVelocity(0, -6);
						sprPlayer.animate(new long[]{150,150,150},9,11,true);
					}else if(x1.getUserData()=="left2_2"||x2.getUserData()=="left2_2"){
						sprPlayer.stopAnimation();
						Log.d("scroll left2-2","passed");
						cameraMoving=true;
						scrolling=true;
						
						collidedGlobalId=100;
						
						nextCenterX=camera.getCenterX()-CAMERA_WIDTH;
						nextCenterY=camera.getCenterY();
						camera.setCenter(camera.getCenterX()-CAMERA_WIDTH, camera.getCenterY());
						pRectBody.setLinearVelocity(-6, 0);
						sprPlayer.animate(new long[]{150,150,150},3,5,true);
					}
//					}else if(x2.getUserData()=="right2_2"){
//						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(6, 0);
//						sprPlayer.animate(new long[]{150,150,150},6,8,true);
//					}else if(x2.getUserData()=="down2_2"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, 6);
//						sprPlayer.animate(new long[]{150,150,150},0,2,false);
//					}else if(x2.getUserData()=="up2_3"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, -6);
//						sprPlayer.animate(new long[]{150,150,150},9,11,true);
//					}else if(x2.getUserData()=="left2_3"){
//						nextCenterX=camera.getCenterX()-CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()-CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(-6, 0);
//						sprPlayer.animate(new long[]{150,150,150},3,5,true);
//					}else if(x2.getUserData()=="right2_3"){
//						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(6, 0);
//						sprPlayer.animate(new long[]{150,150,150},6,8,true);
//					}else if(x2.getUserData()=="down2_3"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, 6);
//						sprPlayer.animate(new long[]{150,150,150},0,2,false);
//					}else if(x2.getUserData()=="up2_4"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, -6);
//						sprPlayer.animate(new long[]{150,150,150},9,11,true);
//					}else if(x2.getUserData()=="left2_4"){
//						nextCenterX=camera.getCenterX()-CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()-CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(-6, 0);
//						sprPlayer.animate(new long[]{150,150,150},3,5,true);
//					}else if(x2.getUserData()=="right2_4"){
//						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(6, 0);
//						sprPlayer.animate(new long[]{150,150,150},6,8,true);
//					}else if(x2.getUserData()=="down2_4"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, 6);
//						sprPlayer.animate(new long[]{150,150,150},0,2,false);
//					}else if(x2.getUserData()=="up2_5"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, -6);
//						sprPlayer.animate(new long[]{150,150,150},9,11,true);
//					}else if(x2.getUserData()=="left2_5"){
//						nextCenterX=camera.getCenterX()-CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()-CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(-6, 0);
//						sprPlayer.animate(new long[]{150,150,150},3,5,true);
//					}else if(x2.getUserData()=="right2_5"){
//						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(6, 0);
//						sprPlayer.animate(new long[]{150,150,150},6,8,true);
//					}else if(x2.getUserData()=="down2_5"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, 6);
//						sprPlayer.animate(new long[]{150,150,150},0,2,false);
//					}else if(x2.getUserData()=="up3_1"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, -6);
//						sprPlayer.animate(new long[]{150,150,150},9,11,true);
//					}else if(x2.getUserData()=="left3_1"){
//						nextCenterX=camera.getCenterX()-CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()-CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(-6, 0);
//						sprPlayer.animate(new long[]{150,150,150},3,5,true);
//					}else if(x2.getUserData()=="right3_1"){
//						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(6, 0);
//						sprPlayer.animate(new long[]{150,150,150},6,8,true);
//					}else if(x2.getUserData()=="down3_1"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, 6);
//						sprPlayer.animate(new long[]{150,150,150},0,2,false);
//					}else if(x2.getUserData()=="up3_2"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, -6);
//						sprPlayer.animate(new long[]{150,150,150},9,11,true);
//					}else if(x2.getUserData()=="left3_2"){
//						nextCenterX=camera.getCenterX()-CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()-CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(-6, 0);
//						sprPlayer.animate(new long[]{150,150,150},3,5,true);
//					}else if(x2.getUserData()=="right3_2"){
//						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(6, 0);
//						sprPlayer.animate(new long[]{150,150,150},6,8,true);
//					}else if(x2.getUserData()=="down3_2"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, 6);
//						sprPlayer.animate(new long[]{150,150,150},0,2,false);
//					}else if(x2.getUserData()=="up3_3"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, -6);
//						sprPlayer.animate(new long[]{150,150,150},9,11,true);
//					}else if(x2.getUserData()=="left3_3"){
//						nextCenterX=camera.getCenterX()-CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()-CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(-6, 0);
//						sprPlayer.animate(new long[]{150,150,150},3,5,true);
//					}else if(x2.getUserData()=="right3_3"){
//						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(6, 0);
//						sprPlayer.animate(new long[]{150,150,150},6,8,true);
//					}else if(x2.getUserData()=="down3_3"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, 6);
//						sprPlayer.animate(new long[]{150,150,150},0,2,false);
//					}else if(x2.getUserData()=="up3_4"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, -6);
//						sprPlayer.animate(new long[]{150,150,150},9,11,true);
//					}else if(x2.getUserData()=="left3_4"){
//						nextCenterX=camera.getCenterX()-CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()-CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(-6, 0);
//						sprPlayer.animate(new long[]{150,150,150},3,5,true);
//					}else if(x2.getUserData()=="right3_4"){
//						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(6, 0);
//						sprPlayer.animate(new long[]{150,150,150},6,8,true);
//					}else if(x2.getUserData()=="down3_4"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, 6);
//						sprPlayer.animate(new long[]{150,150,150},0,2,false);
//					}else if(x2.getUserData()=="up3_5"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, -6);
//						sprPlayer.animate(new long[]{150,150,150},9,11,true);
//					}else if(x2.getUserData()=="left3_5"){
//						nextCenterX=camera.getCenterX()-CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()-CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(-6, 0);
//						sprPlayer.animate(new long[]{150,150,150},3,5,true);
//					}else if(x2.getUserData()=="right3_5"){
//						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(6, 0);
//						sprPlayer.animate(new long[]{150,150,150},6,8,true);
//					}else if(x2.getUserData()=="down3_5"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, 6);
//						sprPlayer.animate(new long[]{150,150,150},0,2,false);
//					}else if(x2.getUserData()=="up4_1"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, -6);
//						sprPlayer.animate(new long[]{150,150,150},9,11,true);
//					}else if(x2.getUserData()=="left4_1"){
//						nextCenterX=camera.getCenterX()-CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()-CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(-6, 0);
//						sprPlayer.animate(new long[]{150,150,150},3,5,true);
//					}else if(x2.getUserData()=="right4_1"){
//						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(6, 0);
//						sprPlayer.animate(new long[]{150,150,150},6,8,true);
//					}else if(x2.getUserData()=="down4_1"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, 6);
//						sprPlayer.animate(new long[]{150,150,150},0,2,false);
//					}else if(x2.getUserData()=="up4_2"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, -6);
//						sprPlayer.animate(new long[]{150,150,150},9,11,true);
//					}else if(x2.getUserData()=="left4_2"){
//						nextCenterX=camera.getCenterX()-CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()-CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(-6, 0);
//						sprPlayer.animate(new long[]{150,150,150},3,5,true);
//					}else if(x2.getUserData()=="right4_2"){
//						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(6, 0);
//						sprPlayer.animate(new long[]{150,150,150},6,8,true);
//					}else if(x2.getUserData()=="down4_2"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, 6);
//						sprPlayer.animate(new long[]{150,150,150},0,2,false);
//					}else if(x2.getUserData()=="up4_3"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, -6);
//						sprPlayer.animate(new long[]{150,150,150},9,11,true);
//					}else if(x2.getUserData()=="left4_3"){
//						nextCenterX=camera.getCenterX()-CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()-CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(-6, 0);
//						sprPlayer.animate(new long[]{150,150,150},3,5,true);
//					}else if(x2.getUserData()=="right4_3"){
//						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(6, 0);
//						sprPlayer.animate(new long[]{150,150,150},6,8,true);
//					}else if(x2.getUserData()=="down4_3"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, 6);
//						sprPlayer.animate(new long[]{150,150,150},0,2,false);
//					}else if(x2.getUserData()=="up4_4"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, -6);
//						sprPlayer.animate(new long[]{150,150,150},9,11,true);
//					}else if(x2.getUserData()=="left4_4"){
//						nextCenterX=camera.getCenterX()-CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()-CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(-6, 0);
//						sprPlayer.animate(new long[]{150,150,150},3,5,true);
//					}else if(x2.getUserData()=="right4_4"){
//						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(6, 0);
//						sprPlayer.animate(new long[]{150,150,150},6,8,true);
//					}else if(x2.getUserData()=="down4_4"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, 6);
//						sprPlayer.animate(new long[]{150,150,150},0,2,false);
//					}else if(x2.getUserData()=="up4_5"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, -6);
//						sprPlayer.animate(new long[]{150,150,150},9,11,true);
//					}else if(x2.getUserData()=="left4_5"){
//						nextCenterX=camera.getCenterX()-CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()-CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(-6, 0);
//						sprPlayer.animate(new long[]{150,150,150},3,5,true);
//					}else if(x2.getUserData()=="right4_5"){
//						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(6, 0);
//						sprPlayer.animate(new long[]{150,150,150},6,8,true);
//					}else if(x2.getUserData()=="down4_5"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, 6);
//						sprPlayer.animate(new long[]{150,150,150},0,2,false);
//					}else if(x2.getUserData()=="up5_1"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, -6);
//						sprPlayer.animate(new long[]{150,150,150},9,11,true);
//					}else if(x2.getUserData()=="left5_1"){
//						nextCenterX=camera.getCenterX()-CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()-CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(-6, 0);
//						sprPlayer.animate(new long[]{150,150,150},3,5,true);
//					}else if(x2.getUserData()=="right5_1"){
//						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(6, 0);
//						sprPlayer.animate(new long[]{150,150,150},6,8,true);
//					}else if(x2.getUserData()=="down5_1"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, 6);
//						sprPlayer.animate(new long[]{150,150,150},0,2,false);
//					}else if(x2.getUserData()=="up5_2"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, -6);
//						sprPlayer.animate(new long[]{150,150,150},9,11,true);
//					}else if(x2.getUserData()=="left5_2"){
//						nextCenterX=camera.getCenterX()-CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()-CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(-6, 0);
//						sprPlayer.animate(new long[]{150,150,150},3,5,true);
//					}else if(x2.getUserData()=="right5_2"){
//						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(6, 0);
//						sprPlayer.animate(new long[]{150,150,150},6,8,true);
//					}else if(x2.getUserData()=="down5_2"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, 6);
//						sprPlayer.animate(new long[]{150,150,150},0,2,false);
//					}else if(x2.getUserData()=="up5_3"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, -6);
//						sprPlayer.animate(new long[]{150,150,150},9,11,true);
//					}else if(x2.getUserData()=="left5_3"){
//						nextCenterX=camera.getCenterX()-CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()-CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(-6, 0);
//						sprPlayer.animate(new long[]{150,150,150},3,5,true);
//					}else if(x2.getUserData()=="right5_3"){
//						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(6, 0);
//						sprPlayer.animate(new long[]{150,150,150},6,8,true);
//					}else if(x2.getUserData()=="down5_3"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, 6);
//						sprPlayer.animate(new long[]{150,150,150},0,2,false);
//					}else if(x2.getUserData()=="up5_4"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, -6);
//						sprPlayer.animate(new long[]{150,150,150},9,11,true);
//					}else if(x2.getUserData()=="left5_4"){
//						nextCenterX=camera.getCenterX()-CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()-CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(-6, 0);
//						sprPlayer.animate(new long[]{150,150,150},3,5,true);
//					}else if(x2.getUserData()=="right5_4"){
//						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(6, 0);
//						sprPlayer.animate(new long[]{150,150,150},6,8,true);
//					}else if(x2.getUserData()=="down5_4"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, 6);
//						sprPlayer.animate(new long[]{150,150,150},0,2,false);
//					}else if(x2.getUserData()=="up5_5"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()-CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()-CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, -6);
//						sprPlayer.animate(new long[]{150,150,150},9,11,true);
//					}else if(x2.getUserData()=="left5_5"){
//						nextCenterX=camera.getCenterX()-CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()-CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(-6, 0);
//						sprPlayer.animate(new long[]{150,150,150},3,5,true);
//					}else if(x2.getUserData()=="right5_5"){
//						nextCenterX=camera.getCenterX()+CAMERA_WIDTH;
//						nextCenterY=camera.getCenterY();
//						camera.setCenter(camera.getCenterX()+CAMERA_WIDTH, camera.getCenterY());
//						pRectBody.setLinearVelocity(6, 0);
//						sprPlayer.animate(new long[]{150,150,150},6,8,true);
//					}else if(x2.getUserData()=="down5_5"){
//						nextCenterX=camera.getCenterX();
//						nextCenterY=camera.getCenterY()+CAMERA_HEIGHT;
//						camera.setCenter(camera.getCenterX(), camera.getCenterY()+CAMERA_HEIGHT);
//						pRectBody.setLinearVelocity(0, 6);
//						sprPlayer.animate(new long[]{150,150,150},0,2,false);
//					}
				}
			}

			@Override
			public void endContact(Contact contact) {
				// TODO Auto-generated method stub
				final Body x1=contact.getFixtureA().getBody();
				final Body x2=contact.getFixtureB().getBody();
				
//				if((x1.getUserData()=="enemy"&&x2.getUserData()=="player")||(x2.getUserData()=="enemy"&&x1.getUserData()=="player")){
//					MainActivity.this.runOnUiThread(new Runnable(){
//
//						@Override
//						public void run() {
//							// TODO Auto-generated method stub
//							Toast.makeText(getApplicationContext(), "enemy touched", Toast.LENGTH_SHORT).show();
//						}
//						
//					});
//				}
				
				Object obj=x1.getUserData();
				Object obj2=x2.getUserData();
				if(obj instanceof JSONObject){
					JSONObject temp=(JSONObject)obj;
//					collidedGlobalId=temp.optInt("globalId");
				}
				
				if(obj2 instanceof JSONObject){
					JSONObject temp=(JSONObject)obj2;
//					collidedGlobalId=temp.optInt("globalId");
				}
					
					if((x2.getUserData()=="player"||x1.getUserData()=="player")&&(x2.getUserData()!="enemy"&&x1.getUserData()!="enemy")&&collidedGlobalId<23){
						objectCollided=false;
						justObtained=false;
						collidedGlobalId=100;
					}
				
				if((x2.getUserData()!="enemy"&&x1.getUserData()!="enemy")&&((x1.getUserData()=="wall"&&x2.getUserData()=="player")||(x2.getUserData()=="wall"&&x1.getUserData()=="player"))){
					wallCollided=false;
					collidedGlobalId=100;
				}
				
			}

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
				// TODO Auto-generated method stub

			}
			
		};
		return cl;
	}

	@Override
	protected synchronized void onResume() {
		// TODO Auto-generated method stub
		if(this.mEngine != null)
		super.onResume();
		System.gc();
		if(this.isGameLoaded()){
			bgm.play();
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(this.isGameLoaded()){
			bgm.pause();
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if(this.isGameLoaded()){
			System.exit(0);
		}
	}

	//敵キャラを動かすメソッド
	private void moveEnemy(){
		enemyHandler=new TimerHandler(enemyMoveSec, true, new ITimerCallback(){

			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				// TODO Auto-generated method stub
				enemyDestinationX=ran.nextInt(980);
				enemyDestinationY=ran.nextInt(560);
				
				currentEnemyRectX=eRectBody.getPosition().x;
				currentEnemyRectY=eRectBody.getPosition().y;
				
				enemyBodyDiffX=enemyDestinationX-currentEnemyRectX*PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
				enemyBodyDiffY=enemyDestinationY-currentEnemyRectY*PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
				
				float ratioX = (enemyBodyDiffX / Math.abs(enemyBodyDiffX)) * Math.min(1,Math.abs(enemyBodyDiffX / enemyBodyDiffY));
				float ratioY = (enemyBodyDiffY / Math.abs(enemyBodyDiffY)) * Math.min(1,Math.abs(enemyBodyDiffY / enemyBodyDiffX));
				
				eRectBody.setLinearVelocity(new Vector2((ratioX*120)/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,(ratioY*120)/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT));
				
				if(Math.abs(enemyBodyDiffX)>Math.abs(enemyBodyDiffY)){
					if(enemyBodyDiffX>0){
						sprEnemy.animate(new long[]{150,150,150},6,8,true);
					}else{
						sprEnemy.animate(new long[]{150,150,150},3,5,true);
					}
				}else{
					if(enemyBodyDiffY>0){
						sprEnemy.animate(new long[]{150,150,150},0,2,true);
					}else{
						sprEnemy.animate(new long[]{150,150,150},9,11,true);
					}
				}
				
//				enemyMove=false;
			}
			
		});
	}
	
	//ゴールした後、3秒後にゲームを初期に戻すメソッド
	private void resetGame(){
		goalAchieved=false;
		resetHandler=new TimerHandler(3f, false, new ITimerCallback(){

			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				// TODO Auto-generated method stub
				pool.play(reset_id, 7.0F, 7.0F, 1, 0, 1.0F);
				
				sprIconKey.setVisible(false);
				sprIconSuitcase.setVisible(false);
				sprIconDocument.setVisible(false);
				sprGoal.setVisible(false);
				keyPosition.set(23, 0);
				suitcasePosition.set(23, 0);
				documentPosition.set(23, 0);
				putKeyObjects();
				
				//カメラ、player,enemyを初期位置に戻す
				camera.setCenterDirect(CAMERA_WIDTH/2, CAMERA_HEIGHT/2);
				
				pRectBody.setTransform(spawnX/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, spawnY/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 0);
				eRectBody.setTransform(enemySpawnX/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, enemySpawnY/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 0);
				
				scopeModeOn=false;
				wallCollided=false;
				objectCollided=false;
				playerMoving=false;
				pathFinding=false;
				cameraMoving=false;
				scrolling=false;
				justObtained=false;
				enemyMove=false;
				objectTargeted=false;
				floorTargeted=false;
				alertOpened=false;
				
				if(pointerShown==true){
					scene.detachChild(sprPointer);
					pointerShown=false;
				}
				
				if(objectHighlighted==true){
					scene.detachChild(highlight);
					objectHighlighted=false;
				}
			}
			
		});
		
		scene.registerUpdateHandler(resetHandler);
	}

}
