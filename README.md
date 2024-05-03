# SealEngine

## Что это? Зачем оно надо?
Данный движок приван взять на себя задачи:
 1. там, где это возможно, избавить пользователя от вызовов низкоуровневых и не интуитивный методов API OpenGL
 2. взять на себя всю работу с видео памятью
 3. предосавить набор обьектов движка, сделать  api обьектно - ориентированным
 4. предоставить абстракицию над классом OpenGLRenderer, сделав возможным создание аналогов activity
 5. обеспечить быстрый старт и легкую разработку прототипа за счет большого количества дефолтных функций

Он может работать с любым glSurfaceView, ниже рассмотрено его использование в полноэкранном режиме
 
## Импорт библиотеки в проект
 1. Создаем пустой проект в Android studio (c пустой дефолтной MainActivity) и скачиваем последнюю версию с репозитория (см релизы)
 2. в проекте в папку `app/libs` копируем файл, пкм, add as android library
 3. ждем
 4. ещё ждем
 5. ура. Теперь открываем MainActivity. Нужно из нее открыть главный реднерер, который позже передаст управление движку и вашему коду. Чтобы это сделать, отредактируем activity:
`Engine engine = new Engine(); //we are unable to make it static because it is impossible to use android context in static way`

 перегрузим методы:

 
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        GLSurfaceView v = engine.onCreate(this, unused -> new MainRenderer(), false);
        setContentView(v);
        assert v != null;
        v.setOnTouchListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        engine.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        engine.onResume();
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return Engine.onTouch(v, event);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

 6. Теперь создаим класс MainRenderer. Это будет входная точка в наш проект. Навзние класса можно изменить в строке `GLSurfaceView v = engine.onCreate(this, unused -> new MainRenderer(), false);`  Данный класс является классом страницы, он обязательно должен implement `GamePageInterface`. Конструтор странц может быть любым, но для входной точки обязательно наличие пустого (в процем, мы сами вызываем его из MainActivity). После переопределения всех функций движок готов к использованию

 ## Устройство движка (коротко)
В основе всего лежит идея многостраничного приложения, в котором  каждой страницы есть свои локальные переменные, удаляемые при ее закртытии. Такой подход позволит абстрагироваться от других страниц, экономить RAM и VRAM, не засорять пространство имён
GamePageInterface является центральной сущностью движка и устроен следующим образом:

	package com.seal.gl_engine;
	
	public interface GamePageInterface {
	    public void draw();
	    public void touchStarted();
	    public void touchMoved();
	    public void touchEnded();
	}


не спрашивайте почему тюлень. Мем.
Мы видим, что получив досутп к нашем листенерам, движок отслеживает открытие страницы, вызыввет  draw каждый кадр и колбеки на тач
Также движок отслеживает использование видео памяти и автоматически удаляет оттуда объекты когда они не нужны
Для этого у каждого объекта есть ссылка на страницу создателя (поэтому, при создании объектов передается this), и при каждом запуске startNewPage происходит проверка, в случае несовпадения названия класса открытой страницы и класса создателя объекта, все используемые видео-ресурсы удаляются, а сам объект становится null чтобы не было соблазна его использовать дальше.

**Объект движка не будет удален после закрытия вашей страницы сборщиком мусора сразу, так как движок ещё какое-то время будет хранить на него ссылку.** 

Если нужно, чтобы объект не удалялся каждый раз (напрмер, тяжелый меш), то его нужно объявить static, а в вместо this предать null.

**если не static объекту передать null, это приведет к утечке видео памяти и непредсказуемым последствиям**

## Создание и использование шейдера
Движок поддерживает создание классической пары vertex+fragment:

    shader = new Shader(com.example.gl_engine.R.raw.vertex_shader,com.example.gl_engine.R.raw.fragment_shader, this, new MainShaderAdaptor());//create default example shader
так и использование геометрического шейдера:

    shader = new Shader(vertex_shader, geom_shader, fragment_shader, this, new MainShaderAdaptor());

Шейдеры рекомендуется объявлять final:

    private final Shader shader;

Чтобы применить шейдер:

	applyShader(shader);//static method of class ShaderUtils

Логи о компиляции будут выводиться с тегом Info. Отсутсвие логов - признак успешной компиляции.
При примении шейдера все объекты, унаследованные от ShaderData автоматически прогрузят свои значения туда (исключение  - Material).

**Остальные объекты, такие как: CameraSettings, ProectionMatrixSettings, матрица преобразования, Material требуют ручного вызова функции отправки данных в шейдер.**


## Передача данных в шейдер
Для этих целей введены адапторы.
В движке есть набор стандартных адапторов с интуитивными названиями, которые рекомендуется использовать, но при желании можно разработать свой.
Для этой цели нужно унаследоваться от абстрактного класс **Adaptor**.
Внутри данного класса хранится список всех ShaderData и id шейдера (выдается API при компиляции).
Сам абстрактный класс отвечает за работу с ShaderData, её перегружать не нужно.
Нужно перегрузить следующие методы:

	public abstract int bindData(Face faces[]);//отправить массив вершин без сохранения в буфер
 
    public abstract int bindData(Face faces[], VertexBuffer vertexBuffer);//отправка в vbo с разметкой в vao

    public abstract void updateLocations();//обновить все используемые id шейдерных переменных

 	//геттеры соответствующих значений
  
    public abstract int getTransformMatrixLocation();

    public abstract int getCameraLocation();

    public abstract int getProjectionLocation();

    public abstract int getTextureLocation();

    public abstract int getNormalTextureLocation();

    public abstract int getNormalMapEnableLocation();

    public abstract int getCameraPosLlocation();

пример реализации см в MainShaderAdator, LightShaderAdaptor, SkyboxAdaptor.

## CameraSettings
Класс настроек камеры.
Содежрит в себе набор переменных, настройки камеры происходят с помощью

	Matrix.setLookAtM(mViewMatrix, 0, cam.eyeX, cam.eyeY, cam.eyeZ, cam.centerX, cam.centerY, cam.centerZ, cam.upX, cam.upY, cam.upZ);

Соответствующие переменные отвечают за положение, направление камеры и направление UP-вектора.
Автоматически настройки камеры не применяются, нужно вызвать 

    applyCameraSettings(cameraSettings);
Функции resetFor3d(), resetFor2d() сбрасывают значения переменных на дефолтные для рисования в одном из режимов с учётом ориентации устройства.
## ProjectionMatrixSettings
Класс настроек матрицы проекци.
Автоматически настройки не применяются, нужно вызвать 

	applyProjectionMatrix(projectionMatrixSettings, boolean perspectiveEnabled);
 или
 
 	applyProjectionMatrix(projectionMatrixSettings);//perspectiveEnabled = true
Класс поддерживает настройку границ основания призмы проекции, а также положения ближнего и дальнего сечений.

Функции resetFor3d(), resetFor2d() сбрасывают значения переменных на дефолтные для рисования в одном из режимов с учётом ориентации устройства.

## Матрица преобразований
Класс матрицы преобразований не реализован. Рекомендуется использовать матрицу <code>float[16]</code> и функции из пакета <code>Android.OpenGL.Matrix.</code>
Для создания единичной матрицы используется <code>resetTranslateMatrix(float[16])</code> (можно передать существующий массив чтобы не выделять новую память), для применения - <code>applyMatrix(float[16]);</code>

## Polygon
Класс для отображения прямоугольной текстуры в любом месте и положения 3д пространства (можно использовать в 2д режиме, но для этого есть класс SimplePolygon).
Конструктор на вход принимает
+ `Function<List<Object>, PImage> redrawFunction` - функция, которая будет вызвана автоматически каждый раз когда будет требоваться перерисовка изображения полигона
+ `bool saveMemory` - не держать изображение текстуры в памяти android
+ `int paramSize` - длина списка параметров в функции перерисовки (можно изменить с помощью newParamsSize)
+   класс создателя
   
`void prepareAndDraw(Point a, Point b, Point c)` - отрисовка по 3м точкам.

`prepareAndDraw(Point a, Point b, float texx, float texy, float teexa, float texb)` - deprecated

`void setRedrawNeeded` - перед началом следующего цикла отрисовки вызвать (или нет) функцию перерисовки (по умолчанию вызывается при перезходе в приложение и после создания объекта полигона)

`void delete()` - освобождает память от Bitmap текстуры и отложенно удаляет текстуру из видео памяти (её удалит движок при переключении страницы)    

`void redrawNow()` - вызов перерисовки в ручную


