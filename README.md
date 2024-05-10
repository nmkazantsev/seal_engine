# SealEngine

## Что это? Зачем оно надо?
Данный движок приван:
 1. Там, где это возможно, избавить пользователя от вызовов низкоуровневых и неинтуитивный методов API OpenGL.
 2. Взять на себя всю работу с видеопамятью.
 3. Предосавить набор обьектов движка, сделать  api обьектно-ориентированным.
 4. Предоставить абстракицию над классом OpenGLRenderer, сделав возможным создание аналогов activity.
 5. Обеспечить быстрый старт и легкую разработку прототипа, за счет большого количества дефолтных функций.

Он может работать с любым glSurfaceView, ниже рассмотрено его использование в полноэкранном режиме.
 
## Импорт библиотеки в проект
 1. Создаем пустой проект в Android studio (c пустой дефолтной MainActivity) и скачиваем последнюю версию с репозитория (см релизы).
 2. Впроекте создаем в папку `app/libs`, копируем файл, пкм, add as android library.
 3. Ждем.
 4. Ещё ждем.
 5. Ура.
 6. Теперь открываем MainActivity. Нужно из нее открыть главный реднерер, который позже передаст управление движку и вашему коду. Чтобы это сделать, отредактируем activity:
`Engine engine = new Engine(); //we are unable to make it static because it is impossible to use android context in static way`

 Перегрузим методы:

 
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

 7. Теперь создаим класс MainRenderer. Это будет входная точка в наш проект. Навзние класса можно изменить в строке `GLSurfaceView v = engine.onCreate(this, unused -> new MainRenderer(), false);`  Данный класс является классом страницы, он обязательно должен implement `GamePageInterface`. Конструтор странц может быть любым, но для входной точки обязательно наличие пустого (в процем, мы сами вызываем его из MainActivity). После переопределения всех функций движок готов к использованию.

## Устройство движка (коротко)
В основе всего лежит идея многостраничного приложения, в котором  каждой страницы есть свои локальные переменные, удаляемые при ее закртытии. Такой подход позволит абстрагироваться от других страниц, экономить RAM и VRAM, не засорять пространство имён.
GamePageInterface является центральной сущностью движка и устроен следующим образом:

	package com.seal.gl_engine;
	
	public interface GamePageInterface {
	    public void draw();
	    public void touchStarted();
	    public void touchMoved();
	    public void touchEnded();
	}


Не спрашивайте почему тюлень. Мем.
Мы видим, что получив досутп к нашем листенерам, движок отслеживает открытие страницы, вызыввет draw и колбеки на тач каждый кадр.
Так же движок отслеживает использование видеопамяти и автоматически удаляет оттуда объекты, когда они не нужны.
Для этого у каждого объекта есть ссылка на страницу создателя (поэтому, при создании объектов передается this), и при каждом запуске startNewPage происходит проверка, в случае несовпадения названия класса открытой страницы и класса создателя объекта, все используемые видеоресурсы удаляются, а сам объект становится null, чтобы не было соблазна его использовать дальше.

**Объект движка не будет удален после закрытия вашей страницы сборщиком мусора сразу, так как движок ещё какое-то время будет хранить на него ссылку.** 

Если нужно, чтобы объект не удалялся каждый раз (напрмер, тяжелый меш), то его нужно объявить static, а в вместо this предать null.

**Если не static объекту передать null, это приведет к утечке видеопамяти и непредсказуемым последствиям.**

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
При примении шейдера все объекты, унаследованные от ShaderData автоматически прогрузят свои значения туда (исключение - Material).

**Остальные объекты, такие как: CameraSettings, ProectionMatrixSettings, матрица преобразования, Material требуют ручного вызова функции отправки данных в шейдер.**


## Передача данных в шейдер
Для этих целей введены адапторы.
В движке есть набор стандартных адапторов с интуитивными названиями, которые рекомендуется использовать, но при желании можно разработать свой.
Для этой цели нужно унаследоваться от абстрактного класса **Adaptor**.
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

Пример реализации см в MainShaderAdator, LightShaderAdaptor, SkyboxAdaptor.

## встроенные переменные и функции

`long millis()` - время в миллисекундах с момента запуска приложения.

`long pageMillis()` - время в миллисекундах с момента запуска текущей страницы.

`float fps` - значение текущей частоты кадров.

# Преобразования вершин и инструменты работы с ними

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
 
 	applyProjectionMatrix(projectionMatrixSettings); // perspectiveEnabled = true;

Класс поддерживает настройку границ основания призмы проекции, а также положения ближнего и дальнего сечений.

Функции resetFor3d(), resetFor2d() сбрасывают значения переменных на дефолтные для рисования в одном из режимов с учётом ориентации устройства.

## Матрица преобразований
Класс матрицы преобразований не реализован. Рекомендуется использовать матрицу <code>float[16]</code> и функции из пакета <code>Android.OpenGL.Matrix.</code>
Для создания единичной матрицы используется <code>resetTranslateMatrix(float[16])</code> (можно передать существующий массив чтобы не выделять новую память), для применения - <code>applyMatrix(float[16]);</code>

## Polygon
Класс для отображения прямоугольной текстуры в любом месте и положении 3д пространства (можно использовать в 2д режиме, но для этого есть класс SimplePolygon).
Конструктор на вход принимает:
+ `Function<List<Object>, PImage> redrawFunction` - функция, которая будет вызвана автоматически каждый раз когда будет требоваться перерисовка изображения полигона;
+ `bool saveMemory` - не держать изображение текстуры в памяти android;
+ `int paramSize` - длина списка параметров в функции перерисовки (можно изменить с помощью newParamsSize);
+ класс создателя;
   
`void prepareAndDraw(Point a, Point b, Point c)` - отрисовка по 3м точкам.

`prepareAndDraw(Point a, Point b, float texx, float texy, float teexa, float texb)` - deprecated

`void setRedrawNeeded` - перед началом следующего цикла отрисовки вызвать (или нет) функцию перерисовки (по умолчанию вызывается при перезходе в приложение и после создания объекта полигона)

`void delete()` - освобождает память от Bitmap текстуры и отложенно удаляет текстуру из видео памяти (её удалит движок при переключении страницы)    

`void redrawNow()` - вызов перерисовки в ручную


## SimplePolygon
Наследуется от класса Polygon, предназначен для 2д рисования. 
Конструктор аналогичный.

`void prepareAndDraw(float rot, float x, float y, float a, float b, float z)` - отрисовывает на прямоугольинке, левый верхний угол в положении x,y, размеры a,b, потом поворачивает прямоугольник на r радиан по часовой. z - высота над плоскостью  z=0. **Идеально для рисования танчика в 2д.** Раньше это был метод класса танка.

`void prepareAndDraw(float x, float y, float b, float z)` -  квадрат стороной b c левым верхниим углом в x,y


## Shape
Класс для работы с 3д мешами. Позволяет добавлять меш, карту нормалей и текстуру. Меш хранится в виде массива чисел (массива объектов Face) и в виде vbo+vao в видеопамяти. **Вся работа с видео памятью автоматизирована**, в том числе подгрузка карты нормалей и вершин.

`Shape(String fileName, String textureFileName, GamePageInterface page)` - конструктор с ассинхронной подгрузкой файла вершин и синхронной (в основном потоке) загрузкой текстуры
В основе загрузчика вершин лежит сторонняя библиотека, а текстура загружается методом переопределения redrawFunction.
Принимаются только триангулированные .obj файлы. При экспорте из blender внимательно следите за положиенем осей и совпадением их с игровым пространством.
Пока вершины не загружены, вызовы рисования shape не дадут никакого резуьтата.

`void addNormalMap(String normalMapFileName)` - загрузка карты нормалей. При этом соответствующая шейдерная переменная (вызов `getNormalMapEnableLocation()` у адаптора) будет установлена в единицу (иначе 0).
**В дефолтном lighting шейдере не происходит отключения перехода в касательное пространство при отключении карты нормалей.**

`void prepareAndDraw()` - отрисовать шейп с учетом текущих настрое камеры, шейдеров и матрицы преобразования. В случае необходимости вызывает функции перерисовки и перезагрузки вершин. 

**Настоятельно не рекомендуется в ручную вызывать функции перерисовки у данного класса** (кроме случаев крайней необходимости).



## Face
Служебный класс. Когда я его писал, как он работает понимали только я и Господь. Теперь это понимает только Господь.
Занимается хранением данных в формате треугольного полигона и выдачей их по требованию в соответствующих форматах для простоты использования в адапторах.
Написание документации на методы данного класса оставляется читателю в качестве самостоятельного несложного упражнения и не несёт практической пользы, так как они нужны исключительно для работы дефолтных шейдеров. 
В списке Face Shape хранит свои данные о вершинах (а также текстурных координатах, нормалях и тп).


## SkyBox
Наследуется от Shape (Добработанный класс). Для своей работы требует наличия в assets cube.obj. 

`SkyBox(String textureFileName, String res, GamePageInterface page)` - первым параметром указывается папка, в которой лежат соответсвующие фрагменты кубической карты. Например, для случая:

	skybox-
		-left.jpg
		-right.jpg
		-top.jpg
		-bottom.jpg
		-front.jpg
	    	-back.jpg
      
вызвать `ShyBox("skybox/","jpg",this);`

`void prepareAndDraw()` - вызввать только после применения шейдеров и повторного примения после этого матриц камеры и проекции  (и, при необходимости, обновления других переменных). Рисует sky box.
В качестве дефолтного шейдера можно использовать skybox_fragment и skybox_vertex.


# Буферы и хранение данных в видео памяти
Для всех рассмотренных ниже объектов работа с памятью автоматизирована, но следует учитывать, что некоторые объекты (напрмер, FrameBuffer) не могут хранить информацию дольше 1 кадра из-за специфики используеомого API.
## FrameBuffer

Название говорит само за себя. Предназначен для внеэкранного реднеринга в текстуру. 

`FrameBuffer createFrameBuffer(int width, int height, GamePageInterface page)` - *не создавать через конструктор. Можно, но не нужно*. Создает и настраивает объект. Принимает на вход размеры текстуры.
**не забыть вызвать glWiewPort перед началом рендеринга.**
 
`static void FrameBufferUtils.connectFrameBuffer(int id)` - подключает FB. На вход можно передать frameBuffer.getFrameBuffer() или id стороннего FB. При повторном подключении буфера можно мотерять предыдущие результаты рендеринга. Аналогично с экранным буфером.

`static void FrameBufferUtils.connectDefaultFrameBuffer()` - отключает внеэкранный рендеринг.

изменение или получение текущих размеров буфера (не будет применено до следующего вызова onRedraw(), автоматизированного или ручного. Вообще, проще удалить и создать новый буфер):

    public void setH(int h) 
    public void setW(int w) 
    public int getWidth() 
    public int getHeight()

`public int getFrameBuffer()` - получить id FrameBuffer

`public int getDepth()` - получить id буфера глубины

`public int getTexture()` - получить id текстуры.

`delete()` - очистка видео памяти.

`drawTexture(Point a, Point b, Point d)` - рисует текстуру на полигоне по 3 точкам (см класс Polygon).


