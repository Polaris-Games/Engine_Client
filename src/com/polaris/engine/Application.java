package com.polaris.engine;

import static com.polaris.engine.render.Renderer.glClearBuffers;
import static com.polaris.engine.render.Renderer.glDefaults;
import static com.polaris.engine.render.Renderer.initializeContent;
import static com.polaris.engine.render.Renderer.updateSize;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowMonitor;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorEnterCallback;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwSetTime;
import static org.lwjgl.glfw.GLFW.glfwSetWindowCloseCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowFocusCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowIconifyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowRefreshCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_TRUE;

import java.nio.DoubleBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorEnterCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.glfw.GLFWWindowIconifyCallback;
import org.lwjgl.glfw.GLFWWindowPosCallback;
import org.lwjgl.glfw.GLFWWindowRefreshCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;

import com.polaris.engine.gui.Gui;
import com.polaris.engine.sound.SoundManager;

public abstract class Application
{
	/**
	 * Instance version of the application's window instance
	 */
	protected static long windowInstance;
	/**
	 * Instance version of the application's mouse position
	 */
	protected static double mouseX;
	/**
	 * Instance version of the application's mouse position
	 */
	protected static double mouseY;

	private GLFWCursorEnterCallback cursorBounds = new GLFWCursorEnterCallback () {

		public void invoke(long window, int entered) 
		{
			cursorMoveBounds(entered == GL_TRUE);
		}
	};
	private GLFWCursorPosCallback cursorPos = new GLFWCursorPosCallback () {

		public void invoke(long window, double xpos, double ypos) 
		{
			cursorMove(xpos, ypos);
		}
	};
	private GLFWMouseButtonCallback cursorButton = new GLFWMouseButtonCallback () {

		public void invoke(long window, int button, int action, int mods)
		{
			cursorClick(button, action);
		}
	};
	private GLFWScrollCallback cursorScroll = new GLFWScrollCallback () {

		public void invoke(long window, double xoffset, double yoffset)
		{
			cursorScroll(xoffset, yoffset);
		}
	};
	private GLFWKeyCallback keyboard = new GLFWKeyCallback () {

		public void invoke(long window, int key, int scancode, int action, int mods)
		{
			keyboardClick(key, action);
		}
	};
	private GLFWWindowCloseCallback windowClose = new GLFWWindowCloseCallback () {

		public void invoke(long window) 
		{
			windowClose();
		}
	};
	private GLFWWindowFocusCallback windowFocus = new GLFWWindowFocusCallback () {

		public void invoke(long window, int focused)
		{
			windowFocus(focused == GL_TRUE);
		}
	};
	private GLFWWindowIconifyCallback windowIconify = new GLFWWindowIconifyCallback () {

		public void invoke(long window, int iconified) 
		{
			windowIconify(iconified == GL_TRUE);
		}
	};
	private GLFWWindowPosCallback windowPos = new GLFWWindowPosCallback () {

		public void invoke(long window, int xpos, int ypos)
		{
			windowPos(xpos, ypos);
		}
	};
	private GLFWWindowRefreshCallback windowRefresh = new GLFWWindowRefreshCallback () {

		public void invoke(long window) 
		{
			windowRefresh();
		}
	};
	private GLFWWindowSizeCallback windowSize = new GLFWWindowSizeCallback () {

		public void invoke(long window, int width, int height)
		{
			windowSize(width, height);
		}
	};

	private Map<Integer, Integer> keyboardPress = new HashMap<Integer, Integer>();
	private Gui currentGui;
	private boolean isRunning = true;
	private int fullscreenMode = 0;
	private SoundManager soundManager = new SoundManager();

	/**
	 * Initializes a window application
	 */
	public void run()
	{
		if(glfwInit() == 0 || !setupWindow())
			return;
		init();

		GL.createCapabilities();
		try
		{
			initializeContent(getResourceLocation());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}

		glDefaults();
		glfwSetTime(0);
		soundManager.start();
		
		while(glfwWindowShouldClose(windowInstance) == 0 && isRunning)
		{
			double delta = glfwGetTime();
			DoubleBuffer mouseBufferX = BufferUtils.createDoubleBuffer(1);
			DoubleBuffer mouseBufferY = BufferUtils.createDoubleBuffer(1);
			glfwSetTime(0);

			if((glfwGetWindowMonitor(windowInstance) == 0) != (fullscreenMode == 0))
			{
				glfwDestroyWindow(windowInstance);
				if(!setupWindow())
					return;
			}

			glfwGetCursorPos(getWindowInstance(), mouseBufferX, mouseBufferY);
			mouseX = mouseBufferX.get();
			mouseY = mouseBufferY.get();
			glfwPollEvents();

			update(delta);

			glClearBuffers();
			render(delta);
			glfwSwapBuffers(windowInstance);
		}
		
		while(soundManager.isAlive());
		
		glfwDestroyWindow(windowInstance);
		GL.destroy();
		glfwTerminate();
	}

	/**
	 * sets up the environment for a window to be created.
	 * @return true for success, false otherwise
	 */
	private boolean setupWindow()
	{
		windowInstance = createWindow();
		if(windowInstance == 0)
		{
			glfwTerminate();
			return false;
		}
		setWindowEvents();
		glfwMakeContextCurrent(windowInstance);
		glfwSwapInterval(1);
		updateSize(windowInstance);
		glfwShowWindow(windowInstance);
		return true;
	}

	/**
	 * creates the window events that handle input and window changes
	 */
	private void setWindowEvents()
	{
		glfwSetCursorEnterCallback(windowInstance, cursorBounds);
		glfwSetCursorPosCallback(windowInstance, cursorPos);
		glfwSetMouseButtonCallback(windowInstance, cursorButton);
		glfwSetScrollCallback(windowInstance, cursorScroll);
		glfwSetKeyCallback(windowInstance, keyboard);
		glfwSetWindowCloseCallback(windowInstance, windowClose);
		glfwSetWindowFocusCallback(windowInstance, windowFocus);
		glfwSetWindowIconifyCallback(windowInstance, windowIconify);
		glfwSetWindowPosCallback(windowInstance, windowPos);
		glfwSetWindowRefreshCallback(windowInstance, windowRefresh);
		glfwSetWindowSizeCallback(windowInstance, windowSize);
	}

	/**
	 * @param newGui : the new gui the screen will adopt, if set to null then the application will close.
	 */
	public void setGui(Gui newGui)
	{
		if(newGui == null)
		{
			close();
			return;
		}
		if(currentGui != null)
		{
			currentGui.close();
		}
		newGui.init();
		currentGui = newGui;
	}

	/**
	 * close the application
	 */
	public void close()
	{
		isRunning = false;
		soundManager.isRunning = false;
	}

	/**
	 * @param fullscreen : 0 for non-fullscreen, 1 for windowed fullscreen, 2 for fullscreen
	 */
	public void setFullscreen(int fullscreen)
	{
		fullscreenMode = fullscreen;
	}

	/**
	 * @return 0 for non-fullscreen, 1 for windowed fullscreen, 2 for fullscreen
	 */
	public int getFullscreenMode()
	{
		return fullscreenMode;
	}

	/**
	 * when the mouse leaves the window, or enters
	 * @param entered : true if mouse enters window
	 */
	protected void cursorMoveBounds(boolean entered) {}

	/**
	 * when the mouse moves in the window
	 * @param mouseX : new mouse x
	 * @param mouseY : new mouse y
	 */
	protected void cursorMove(double mouseX, double mouseY) {}

	/**
	 * when the mouse clicks
	 * <br><b>DON'T CALL super.cursorClick(button, action) UNLESS YOU IMPLEMENT GUI CLASS STRUCTURE</b>
	 * @param button : the mouse button
	 * @param action : type of click, GLFW_PRESS, GLFW_RELEASE, GLFW_REPEAT
	 */
	protected void cursorClick(int button, int action) 
	{
		switch(action)
		{
		case GLFW_PRESS:
			currentGui.mouseClick(button);
			break;
		case GLFW_REPEAT:
			currentGui.mouseHeld(button);
			break;
		case GLFW_RELEASE:
			currentGui.mouseRelease(button);
		}
	}

	/**
	 * when the mouse wheel scrolls
	 * <br><b>DON'T CALL super.cursorScroll(xOffset, yOffset) UNLESS YOU IMPLEMENT GUI CLASS STRUCTURE</b>
	 * @param xOffset : mouse wheel offset x
	 * @param yOffset : mouse wheel offset y
	 */
	protected void cursorScroll(double xOffset, double yOffset) 
	{
		currentGui.mouseScroll(xOffset, yOffset);
	}

	/**
	 * when the keyboard clicks
	 * <br><b>DON'T CALL super.keyboardClick(key, action) UNLESS YOU IMPLEMENT GUI CLASS STRUCTURE</b>
	 * @param key : the key id
	 * @param action : type of click, GLFW_PRESS, GLFW_RELEASE, GLFW_REPEAT
	 */
	protected void keyboardClick(int key, int action) 
	{
		switch(action)
		{
		case GLFW_PRESS:
			int i = currentGui.keyPressed(key);
			if(i > 0)
			{
				keyboardPress.put(key, i);
			}
			break;
		case GLFW_REPEAT:
			int j = keyboardPress.get(key);
			int k = j & 0x0000FFFF;
			j >>= 16;
			if(k-- <= 0)
			{
				currentGui.keyHeld(key, j);
				j++;
				if(k <= 0)
				{
					keyboardPress.remove(key);
					break;
				}
			}
			keyboardPress.put(key, (j << 16) | k);
			break;
		case GLFW_RELEASE:
			currentGui.keyRelease(key);
			keyboardPress.remove(key);
		}
	}

	/**
	 * when the window closes
	 */
	private void windowClose() 
	{
		this.cursorBounds.release();
		this.cursorButton.release();
		this.cursorPos.release();
		this.cursorScroll.release();
		this.keyboard.release();
		this.windowClose.release();
		this.windowFocus.release();
		this.windowIconify.release();
		this.windowPos.release();
		this.windowRefresh.release();
		this.windowSize.release();
		soundManager.isRunning = false;
	}

	/**
	 * when the window focus changes
	 * @param focused : if the window focuses
	 */
	protected void windowFocus(boolean focused) {}

	/**
	 * when the window iconify changes
	 * @param iconified : if the window iconifies
	 */
	protected void windowIconify(boolean iconified) {}

	/**
	 * when the windows position changes
	 * @param xPos : new pos x of window
	 * @param yPos : new pos y of window
	 */
	protected void windowPos(int xPos, int yPos) {}

	/**
	 * when the window is refreshed
	 */
	protected void windowRefresh() {}

	/**
	 * when the windows size changes
	 * @param width : new width
	 * @param height : new height
	 */
	protected void windowSize(int width, int height) 
	{
		updateSize(windowInstance);
	}

	/**
	 * Update method called every n times / second 
	 * <br><b>DON'T CALL super.update(mouseX, mouseY, delta) UNLESS YOU IMPLEMENT GUI CLASS STRUCTURE</b>
	 * @param mouseX : current Mouse Position, updates before method call
	 * @param mouseY : current Mouse Position, updates before method call
	 * @param delta : change in time, measured in actual seconds
	 */
	protected void update(double delta) 
	{
		currentGui.update(delta);
	}

	/**
	 * Render method capped at n times / second
	 * <br><b>DON'T CALL super.render(mouseX, mouseY, delta) UNLESS YOU IMPLEMENT GUI CLASS STRUCTURE</b>
	 * @param mouseX : current Mouse Position, updates before method call
	 * @param mouseY : current Mouse Position, updates before method call
	 * @param delta : change in time, measured in actual seconds
	 */
	protected void render(double delta) 
	{
		currentGui.render(delta);
	}

	/**
	 * initialize window
	 */
	protected abstract void init();

	/**
	 * create the window
	 */
	protected abstract long createWindow();

	protected String getResourceLocation()
	{
		return "resources";
	}

	/**
	 * Set hints to window and others mostly not used with this method
	 * @param target <br><b>WINDOW</b>
	 * <br> - <b>GLFW_FOCUSED</b> : window is focused (D TRUE)
	 * <br> - <b>GLFW_ICONIFIED</b> : window is minimized (D FALSE)
	 * <br> - <b>GLFW_VISIBLE</b> : window is visible (D TRUE)
	 * <br> - <b>GLFW_RESIZEABLE</b> : window is resizeable (D TRUE)
	 * <br> - <b>GLFW_DECORATED</b> : window has borders (D TRUE)
	 * <br> - <b>GLFW_FLOATING</b> : window is always-on-top (D FALSE)
	 * @param value : true or false
	 */
	protected static void setHint(int target, boolean value)
	{
		glfwWindowHint(target, value ? 1 : 0);
	}

	/**
	 * Centers the window and returns its instance long variable
	 * @param width : window width
	 * @param height : window height
	 * @param title : window title
	 * @param monitor : window monitor
	 * @param share : window share
	 * @return
	 */
	protected static long createAndCenter(int width, int height, String title, int monitor, int share)
	{
		long instance = glfwCreateWindow(width, height, title, monitor, share);
		GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(instance, (videoMode.width() - width) / 2, (videoMode.height() - height) / 2);
		return instance;
	}

	/**
	 * @return windows instance
	 */
	public static long getWindowInstance()
	{
		return windowInstance;
	}

	/**
	 * @return mouse position
	 */
	public static double getMouseX()
	{
		return mouseX;
	}

	/**
	 * @return mouse position
	 */
	public static double getMouseY()
	{
		return mouseY;
	}

}
