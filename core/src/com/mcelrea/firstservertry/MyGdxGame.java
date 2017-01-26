package com.mcelrea.firstservertry;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.sun.xml.internal.ws.api.message.ExceptionHasMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class MyGdxGame extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	Socket socket;
	Player player;
	ShapeRenderer shapeRenderer;
	HashMap<String,Player> friendlyPlayers = new HashMap<String,Player>();
	private final float UPDATE_TIME = 1/60f;
	private float timer;

	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
		shapeRenderer = new ShapeRenderer();
		shapeRenderer.setAutoShapeType(true);

		connectSocket();
		configSocketEvents();
	}

	public void updateServer(float delta) {
		timer += delta;
		if(timer >= UPDATE_TIME && player != null && player.hasMoved()) {
			JSONObject data = new JSONObject();
			try {
				data.put("x", player.getX());
				data.put("y", player.getY());
				socket.emit("playerMoved", data);
			} catch(Exception e) {

			}
		}
	}

	private void configSocketEvents() {
		socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				Gdx.app.log("SocketIO", "Connected to Server");
				player = new Player(new Color(1,0,0,1));
			}
		}).on("socketID", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try {
					String id = data.getString("id");
					Gdx.app.log("SocketIO", "My socket id " + id);
				}catch(Exception e) {
					System.out.println(e);
				}
			}
		}).on("newPlayer", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject)args[0];
				try {
					String id = data.getString("id");
					Gdx.app.log("SocketIO", "New player connected " + id);
					friendlyPlayers.put(id,new Player(new Color(0,1,0,1)));
				}catch(Exception e) {
					Gdx.app.log("SocketIO", "Error getting new player");
				}
			}
		}).on("getPlayers", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONArray existingPlayers = (JSONArray)args[0];
				try{
					for(int i=0; i < existingPlayers.length(); i++) {
						Player player = new Player(new Color(0,1,0,1));
						float x = ((Double)existingPlayers.getJSONObject(i).getDouble("x")).floatValue();
						float y = ((Double)existingPlayers.getJSONObject(i).getDouble("y")).floatValue();
						player.setPosition(x,y);
						friendlyPlayers.put(existingPlayers.getJSONObject(i).getString("id"),player);
					}
				}catch(Exception e) {

				}
			}
		}).on("playerDisconnected", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject)args[0];
				try{
					String id = data.getString("id");
					friendlyPlayers.remove(id);
				}catch(Exception e) {

				}
			}
		}).on("playerMoved", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject)args[0];
				try {
					String otherId = data.getString("id");
					Double x = data.getDouble("x");
					Double y = data.getDouble("y");
					if(friendlyPlayers.get(otherId) != null) {
						friendlyPlayers.get(otherId).setPosition(x.floatValue(),y.floatValue());
					}
				} catch(Exception e) {

				}
			}
		});
	}

	private void connectSocket() {
		try {
			socket = IO.socket("http://localhost:8080");
			socket.connect();
		}catch (Exception e) {
			System.out.println(e);
		}
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		handleInput(Gdx.graphics.getDeltaTime());
		updateServer(Gdx.graphics.getDeltaTime());

		batch.begin();
		batch.end();

		shapeRenderer.begin();
		for(HashMap.Entry<String,Player> p: friendlyPlayers.entrySet()) {
			p.getValue().draw(shapeRenderer);
		}
		if(player != null) {
			player.draw(shapeRenderer);
		}
		shapeRenderer.end();
	}

	private void handleInput(float deltaTime) {
		if(player != null) {
			if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
				player.setPosition(player.getX(),
						player.getY() + 200 * deltaTime);
			}
			else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
				player.setPosition(player.getX(),
						player.getY() - 200 * deltaTime);
			}
			else if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
				player.setPosition(player.getX()-200*deltaTime,
						player.getY());
			}
			else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
				player.setPosition(player.getX()+200*deltaTime,
						player.getY());
			}
		}
	}

	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}
}
