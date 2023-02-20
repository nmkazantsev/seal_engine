package com.manateam.glengine3.engine.oldEngine.animshapes;


import static com.manateam.glengine3.utils.Utils.loadImage;

import com.manateam.glengine3.engine.main.images.PImage;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class VideoShape {
    //если ты настолько разжирелся, что нужно больше расширения чем 2048*2048
    //то можно создать анимацию из нескольких последовательных. Эта шутка обеспечит автоматическое
    //переключение между ними. Она просто держит их в массиве и проигрывает по очереди
    AnimShape parts[];
    int currentAnim = 0;

    public boolean playOnce = false;
    public boolean isPlaying = true;
    private String prefix;
    private int startNames, stopNames;
    private boolean isLoaded = false;
    private PImage[] loadedCadrs;
    private WeakReference<VideoShape> link;
    private boolean rePostNeeded = true;
    private float cadrSizeX, cadrSizeY, frameRate;
    private boolean firstStart = true;
    private boolean loadThreadStarted = false;
    private boolean addedLink = false;

    //умеет загружить их с диска, есил имя файлов в формате  smth9.png, где smth - любая строка, 9- любое итерируемое чиисло
    //эту функцию я не тестил
    public VideoShape(String prefix, int start, int stop, float cadrSizeX, float cadrSizeY, float frameRate) {
        parts = new AnimShape[stop - start + 1];
        this.prefix = prefix;
        this.startNames = start;
        this.stopNames = stop;
        this.cadrSizeX = cadrSizeX;
        this.cadrSizeY = cadrSizeY;
        this.frameRate = frameRate;
        link = new WeakReference<VideoShape>(this);
        loadedCadrs = new PImage[stop - start + 1];//создаем массив, сюда в фоне грузим фото, потом перед перрерисовкой не в фоне грузим это в видкопамять
        redrawSetup();
    }

    public void redrawSetup() {
        rePostNeeded = true;
        isLoaded = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!loadThreadStarted) {
                    loadThreadStarted = true;
                    //чтобы были короче имена, сымсла в этом нет
                    int start = startNames;
                    int stop = stopNames;
                    //загрузка в отдельном потоке чтобы меньше лагало основное окно
                    for (int i = start; i <= stop; i++) {
                        loadedCadrs[i - start] = loadImage(prefix + String.valueOf(i) + ".png");
                    }
                    if (!addedLink) {
                        allShapes.add(link);//добавить ссылку на Shape если первый запуск
                        addedLink = true;
                    }
                    isLoaded = true;
                    loadThreadStarted = false;
                }
            }
        }).start();
    }

    private boolean restartNeeded = false;

    public void restart() {
        restartNeeded = true;
    }

    public void pause() {
        this.isPlaying = false;
        parts[currentAnim].isPlaying = false;
        parts[currentAnim].cancelRestart();//на всякий случай елси был запланирован рестарт
    }

    public void resume() {
        this.isPlaying = true;
        parts[currentAnim].isPlaying = true;
    }

    //рисует
    public void draw(float px, float py, float a, float b, float z) {
        if (isLoaded) {
            if (rePostNeeded) {
                rePostNeeded = false;
                for (int i = startNames; i <= stopNames; i++) {
                    //если епрвый запуск то надо создать анимашку, иначе просто обновить
                    if (firstStart) {
                        parts[i - startNames] = new AnimShape(loadImage(prefix + String.valueOf(i) + ".png"), cadrSizeX, cadrSizeY, frameRate, null);
                        //чтобы сразу перекючиться на следующий кусок после завершения этого
                        parts[i - startNames].playOnce = true;
                    } else {
                        parts[i - startNames].uploadImg(loadImage(prefix + String.valueOf(i) + ".png"));
                    }
                }
                firstStart = false;
                for (int i = 0; i < loadedCadrs.length; i++) {
                    loadedCadrs[i].delete();
                }
            }
            if (restartNeeded) {
                isPlaying = true;
                currentAnim = 0;
                restartNeeded = false;
            }
            //рисуем выбранную анимацию
            try {
                parts[currentAnim].draw(px, py, a, b, z);
            } catch (Exception e) {
                redrawSetup();
            }
            //если она уже закончилась
            if ((!parts[currentAnim].isPlaying) && this.isPlaying) {
                parts[currentAnim].restart();//чтобы когда в следущтй раз ее включим, она запустилась
                currentAnim++;
                if (currentAnim == parts.length && playOnce) {
                    currentAnim--;
                    pause();
                    parts[currentAnim].cancelRestart();
                }
                currentAnim %= (parts.length);
            }
        }
    }

    //осовободждает всю память. ОБЯЗАТЕЛЬНО вызвать перед удалением (videoShape=null или videoshape=new VideoShape....) иначе будет дикая утечка.
    //после этого надо вызвать редров или создать новый шейп для дальнейшего проигрывания, иначе вылет.
    public void delete() {
        for (int i = 0; i < parts.length; i++) {
            parts[i].delete();
        }
    }

    //а это для автоматической перерисовки, аналогично шейпам
    private static List<WeakReference<VideoShape>> allShapes = new ArrayList<>();

    public static void redrawAll() {
        Iterator<WeakReference<VideoShape>> iterator = allShapes.iterator();
        while (iterator.hasNext()) {
            WeakReference<VideoShape> e = iterator.next();
            if (e.get() != null) {
                e.get().redrawSetup();
            } else {
                iterator.remove();
            }
        }
    }
}
