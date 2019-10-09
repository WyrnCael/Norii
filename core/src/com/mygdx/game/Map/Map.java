package com.mygdx.game.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.Audio.AudioManager;
import com.mygdx.game.Audio.AudioObserver;
import com.mygdx.game.Audio.AudioSubject;
import com.mygdx.game.Battle.BattleManager;

import Utility.Utility;

public abstract class Map implements AudioSubject{
    private static final String TAG = Map.class.getSimpleName();

    public final static float UNIT_SCALE  = 1/32f;
    public final static int TILE_HEIGHT  = 32;
    public final static int TILE_WIDTH  = 32;
    public static int TILE_HEIGHT_PIXEL  = 32;
    public static int TILE_WIDTH_PIXEL  = 32;
    
    //UI
    protected int uiUnitlistWidth;
	protected int uiUnitlistHeight;
    protected int uiBottomMenuWidth;
    protected int uiBottomMenuHeight;
    
    private Array<AudioObserver> _observers;

    //Map layers
    protected final static String MAP_COLLISION_LAYER = "items";
    protected final static String MAP_SPAWNS_LAYER = "Spawn points";
    protected final static String BACKGROUND_LAYER = "background";
    protected final static String NAVIGATION_LAYER = "navigation";
    
    protected final static String PLAYER_START = "PLAYER_START";

    protected Json _json;
    protected TiledMap _currentMap = null;
    protected TiledMapStage tiledmapstage;
    protected BattleManager battlemanager;
    protected MapLayer _collisionLayer = null;
    protected MapLayer _spawnsLayer = null;
    protected MyNavigationTiledMapLayer  _navLayer = null;
	protected MyPathFinder pathfinder;

	protected MapFactory.MapType _currentMapType;
    
    //props
    protected MapProperties prop;

	protected int mapWidth;
    protected int mapHeight;
    protected int tilePixelWidth;
    protected int tilePixelHeight;

    Map( MapFactory.MapType mapType, String fullMapPath){
        if( fullMapPath == null || fullMapPath.isEmpty() ) {
            Gdx.app.debug(TAG, "Map is invalid");
            return;
        }

        disposeMapAndStage();


        Utility.loadMapAsset(fullMapPath);
        if( Utility.isAssetLoaded(fullMapPath) ) {
            _currentMap = Utility.getMapAsset(fullMapPath);
        }else{
            Gdx.app.debug(TAG, "Map not loaded");
            return;
        }
        
        setupMapProperties();
    	initializeClassVariables(mapType);
		
		//Observers
        this.addObserver(AudioManager.getInstance());
    }
    
    private void setupMapProperties() {
        //setup properties
        prop = _currentMap.getProperties();
        
		mapWidth = prop.get("width", Integer.class);
		mapHeight = prop.get("height", Integer.class);
		tilePixelWidth = prop.get("tilewidth", Integer.class);
		tilePixelHeight = prop.get("tileheight", Integer.class);
		
		updateUIDimensions();
		updatePixelDimensions();
    }

	private void updateUIDimensions() {
		uiUnitlistWidth = 2 * tilePixelWidth;
	    uiUnitlistHeight  = 20 * tilePixelHeight;
	    uiBottomMenuWidth  = 20 * tilePixelWidth;
	    uiBottomMenuHeight  = 3 * tilePixelHeight;
	}
    
    public void updatePixelDimensions() {
		TILE_WIDTH_PIXEL = (Gdx.graphics.getWidth() - uiUnitlistWidth) / mapWidth;
		TILE_HEIGHT_PIXEL = (Gdx.graphics.getHeight() -  uiBottomMenuHeight) / mapHeight;
    }
    
    private void disposeMapAndStage() {
        if( _currentMap != null ){
            _currentMap.dispose();
            if( tiledmapstage != null ){
            	tiledmapstage.dispose();
            }
        }
        if( pathfinder != null ){
        	pathfinder.dispose();
        }
    }
    
    private void initializeClassVariables(MapFactory.MapType mapType) {
        _json = new Json();
        _currentMapType = mapType;
        _observers = new Array<AudioObserver>();
    }
    
    public MyNavigationTiledMapLayer  get_navLayer() {
		return _navLayer;
	}

	public void set_navLayer(MyNavigationTiledMapLayer  _navLayer) {
		this._navLayer = _navLayer;
	}

    public MapLayer getCollisionLayer(){
        return _collisionLayer;
    }

    public TiledMap getCurrentTiledMap() {
        return _currentMap;
    }
    
    public int getMapWidth() {
		return mapWidth;
	}

	public void setMapWidth(int mapWidth) {
		this.mapWidth = mapWidth;
	}

	public int getMapHeight() {
		return mapHeight;
	}

	public void setMapHeight(int mapHeight) {
		this.mapHeight = mapHeight;
	}
	
    public int getUiUnitlistWidth() {
		return uiUnitlistWidth;
	}

	public int getUiUnitlistHeight() {
		return uiUnitlistHeight;
	}

	public int getUiBottomMenuWidth() {
		return uiBottomMenuWidth;
	}

	public int getUiBottomMenuHeight() {
		return uiBottomMenuHeight;
	}
		
    public MyPathFinder getPathfinder() {
		return pathfinder;
	}

	abstract public void unloadMusic();
    abstract public void loadMusic();

    @Override
    public void addObserver(AudioObserver audioObserver) {
        _observers.add(audioObserver);
    }

    @Override
    public void removeObserver(AudioObserver audioObserver) {
        _observers.removeValue(audioObserver, true);
    }

    @Override
    public void removeAllObservers() {
        _observers.removeAll(_observers, true);
    }

    @Override
    public void notify(AudioObserver.AudioCommand command, AudioObserver.AudioTypeEvent event) {
        for(AudioObserver observer: _observers){
            observer.onNotify(command, event);
        }
    }
}
