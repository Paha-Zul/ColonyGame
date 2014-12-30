package com.mygdx.game.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/**
 * Created by Bbent_000 on 12/25/2014.
 */
public class GUI {
    public static BitmapFont font = new BitmapFont();

    public static void Texture(Rectangle rect, Texture texture, SpriteBatch batch){
        batch.draw(texture, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    public static void Texture(float x, float y, float width, float height, Texture texture, SpriteBatch batch){
        batch.draw(texture, x, y, width, height);
    }

    public static void Text(String text, SpriteBatch batch, float x, float y){
        font.draw(batch, text, x, y);
    }

    public static boolean Button(Rectangle rect, Texture texture, SpriteBatch batch){
        batch.draw(texture, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());

        if(rect.contains(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()) && Gdx.input.isButtonPressed(Input.Buttons.LEFT))
            return true;
        return false;
    }

    public static boolean Button(Rectangle rect, Texture texture, String text, SpriteBatch batch){
        batch.draw(texture, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());   //Draw the background texture.
        BitmapFont.TextBounds bounds = font.getBounds(text);                                //Get the bounds of the text
        font.draw(batch, text, rect.getX() + rect.getWidth()/2 - bounds.width/2, rect.getY() + rect.getHeight()/2 + bounds.height/2); //Draw the text
        //Vector2 correctMouseCoords = new Vector2(Gdx.input.getX(), Gdx.input.getY());

        //System.out.println("mouse: "+Gdx.input.getX()+" "+(Gdx.graphics.getHeight() - Gdx.input.getY()) + " rect: "+rect);
        //If our mouse is inside the rect, return true.
        if(rect.contains(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()) && Gdx.input.isButtonPressed(Input.Buttons.LEFT))
            return true;
        return false;
    }

    public static void Label(String text, SpriteBatch batch, float x, float y, boolean centered){
        if(centered){
            BitmapFont.TextBounds bounds = font.getBounds(text);
            font.draw(batch, text, x - bounds.width/2, y + bounds.height/2);
        }else{
            font.draw(batch, text, x, y);
        }
    }

    public static String TextBox(String text, SpriteBatch batch, float x, float y){
        return "";
    }
}
