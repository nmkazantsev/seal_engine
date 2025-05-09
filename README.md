# SealEngine

## Что это? Зачем оно надо?

Данный движок призван:

1. Там, где это возможно, избавить пользователя от вызовов низкоуровневых и неинтуитивных методов  
   API OpenGL.
2. Взять на себя всю работу с видеопамятью.
3. Предосавить набор обьектов движка, сделать api обьектно-ориентированным.
4. Предоставить абстракицию над классом OpenGLRenderer, сделав возможным создание аналогов activity.
5. Обеспечить быстрый старт и легкую разработку прототипа, за счет большого количества дефолтных  
   функций.

Он может работать с любым glSurfaceView, ниже рассмотрено его использование в полноэкранном режиме.

## Импорт библиотеки в проект

1. Создаем пустой проект в Android studio (c пустой дефолтной MainActivity) и скачиваем последнюю  
   версию с репозитория (см релизы).
2. Впроекте создаем в папку `app/libs`, копируем файл, пкм, add as android library.
3. Ждем.
4. Ещё ждем.
5. Ура.
6. Теперь открываем MainActivity. Нужно из нее открыть главный реднерер, который позже передаст  
   управление движку и вашему коду. Чтобы это сделать, отредактируем activity:  
   `Engine engine = new Engine(); //we are unable to make it static because it is impossible to use android context in static way`

Перегрузим методы:

  	public class MainActivity extends Activity implements View.OnTouchListener {
    	Engine engine = new Engine(); //we are unable to make it static because 
        //it is impossible to use
    	//android context in static way

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        GLSurfaceView v = engine.onCreate(this, unused -> new LorRenderer(), true, true, true);
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
        return TouchProcessor.onTouch(v, event);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }}
    


7. Теперь создаим класс MainRenderer. Это будет входная точка в наш проект. Навзние класса можно  
   изменить в
   строке `GLSurfaceView v = engine.onCreate(this, unused -> new MainRenderer(), false);`  
   Данный класс является классом страницы, он обязательно должен implement `GamePageInterface`.  
   Конструтор странц может быть любым, но для входной точки обязательно наличие пустого (в процем,  
   мы сами вызываем его из MainActivity). После переопределения всех функций движок готов к  
   использованию.

## Устройство движка (коротко)

В основе всего лежит идея многостраничного приложения, в котором каждой страницы есть свои
локальные  
переменные, удаляемые при ее закрытии. Такой подход позволит абстрагироваться от других страниц,  
экономить RAM и VRAM, не засорять пространство имён.  
GamePageInterface является центральной сущностью движка и устроен следующим образом:

    package com.seal.gl_engine;        
    public interface GamePageInterface {  
    public abstract void draw();        
    public abstract void onResume();
    public abstract void onPause();
    }  

Не спрашивайте почему тюлень. Мем.  
Мы видим, что получив досутп к нашем листенерам, движок отслеживает открытие страницы, вызыввет
draw  
В моменты открытия/закрытия активити вызываются соответсвующие методы. Они вызываются напрямую из методов активити, откуда запущен движок.
Так же движок отслеживает использование видеопамяти и автоматически удаляет оттуда объекты, когда  
они не нужны.  
Для этого у каждого объекта есть ссылка на страницу создателя (поэтому, при создании объектов  
передается this), и при каждом запуске startNewPage происходит проверка, в случае несовпадения  
названия класса открытой страницы и класса создателя объекта, все используемые видеоресурсы  
удаляются, а сам объект становится null, чтобы не было соблазна его использовать дальше.

**Объект движка не будет удален после закрытия вашей страницы сборщиком мусора сразу, так как
движок  
ещё какое-то время будет хранить на него ссылку.**

Если нужно, чтобы объект не удалялся каждый раз (напрмер, тяжелый меш), то его нужно объявить  
static, а в вместо this предать null.

**Если не static объекту передать null, это приведет к утечке видеопамяти и непредсказуемым  
последствиям.**

## Создание и использование шейдера

Движок поддерживает как создание классической пары vertex+fragment:

    shader = new Shader(com.example.gl_engine.R.raw.vertex_shader,com.example.gl_engine.R.raw.fragment_shader, this, new MainShaderAdaptor());//create default example shader  

так и использование геометрического шейдера:

    shader = new Shader(vertex_shader, geom_shader, fragment_shader, this, new MainShaderAdaptor());  

Шейдеры рекомендуется объявлять final:

    private final Shader shader;  

Чтобы применить шейдер:

    applyShader(shader);//static method of class ShaderUtils  

Логи о компиляции будут выводиться с тегом Info. Отсутсвие логов - признак успешной компиляции.  
При примении шейдера все объекты, унаследованные от ShaderData автоматически прогрузят свои
значения  
туда (исключение - Material).  
В движке в стандартной папке ресурсов есть дефолтные шейдеры для рендеринга, для света и для
работы  
со skyBox.

**Остальные объекты, такие как: CameraSettings, ProectionMatrixSettings, матрица преобразования,  
Material требуют ручного вызова функции отправки данных в шейдер.**

## Передача данных в шейдер

Для этих целей введены адапторы.  
В движке есть набор стандартных адапторов с интуитивными названиями, которые рекомендуется  
использовать, но при желании можно разработать свой.  
Для этой цели нужно унаследоваться от абстрактного класса **Adaptor**.  
Внутри данного класса хранится список всех ShaderData и id шейдера (выдается API при компиляции).  
Сам абстрактный класс отвечает за работу с ShaderData, её перегружать не нужно.  
Нужно перегрузить следующие методы:

    public abstract int bindData(Face faces[]);//отправить массив вершин без сохранения в буфер     public abstract int bindData(Face faces[], VertexBuffer vertexBuffer);//отправка в vbo с разметкой в vao  
  
    public abstract void updateLocations();//обновить все используемые id шейдерных переменных  
    //геттеры соответствующих значений      public abstract int getTransformMatrixLocation();  
  
    public abstract int getCameraLocation();  
    public abstract int getProjectionLocation();  
    public abstract int getTextureLocation();  
    public abstract int getNormalTextureLocation();  
    public abstract int getNormalMapEnableLocation();  
    public abstract int getCameraPosLlocation();  

Пример реализации см в MainShaderAdator, LightShaderAdaptor, SkyboxAdaptor.

## Класс ShaderData

Это абстрактный класс, при реализации значения, находящегося в видеопамяти (uniform переменная)  
нужно от него унаследоваться и переопределить

`void getLocations(int programId)` - загрузка id переменной из шейдера (на вход поступает id  
скомпилированной связки шейдеров)

`void forwardData()` - отправка данных в шейдер

`void delete()` - очистка видеопамяти  
Функции вызываются движком при применении шейдера или смене страницы.

При применении вызываются getLocstions, при смене страницы - delete.  
Смысла удалять uniform переменную нет, но есть смысл удалять ссылки на объект из массивов,
например,  
удалить объект света из массива источников для данной страницы.

Если ссылка на переменную 0, то ничего не произойдёт. Если на переменную ссылаются 2 ссылки, то  
будет записано второе значение, первое перезапишется.

# Преобразования вершин и инструменты работы с ними

## Camera

Класс игровой камеры. Объединяет в себе CameraSettings и ProjectionMatrixSettings (которые
вяляются  
его полями).

Конструктор сразу настраивает камеру для работы с 3д, вызывая соответствующие методы у обоих полей  
класса.

`void apply()` - применние настроек камеры, вызывать перед рисованием объектов. Если перед этим
была  
вызвана resetFor3d(), то перспектива будет включена, если перед этим была вызвана restFor2d() -  
выключена.

`apply(boolean perspectiveEnabled)` - ручное управление перспективой.

**настройками полей класса можно управлять по отдельности, но в большинстве случаев так делать не  
рекомендуется**

Также доступны методы для быстрой передачи данных (копирование) в камеру:

```
     void setPos(PVector pos)
     void SetUpVector(PVector up) 
     void setCenter(PVector center) 
```

## CameraSettings

Класс настроек камеры.  
Содежрит в себе набор переменных, настройки камеры происходят с помощью

    Matrix.setLookAtM(mViewMatrix, 0, cam.eyeX, cam.eyeY, cam.eyeZ, cam.centerX, cam.centerY, cam.centerZ, cam.upX, cam.upY, cam.upZ);  

Соответствующие переменные отвечают за положение, направление камеры и направление UP-вектора.  
Автоматически настройки камеры не применяются, нужно вызвать

    applyCameraSettings(cameraSettings);  

Функции resetFor3d(), resetFor2d() сбрасывают значения переменных на дефолтные для рисования в
одном  
из режимов с учётом ориентации устройства.

## ProjectionMatrixSettings

Класс настроек матрицы проекци.  
Автоматически настройки не применяются, нужно вызвать

    applyProjectionMatrix(projectionMatrixSettings, boolean perspectiveEnabled);  

или

    applyProjectionMatrix(projectionMatrixSettings); // perspectiveEnabled = true;  

Класс поддерживает настройку границ основания призмы проекции, а также положения ближнего и
дальнего  
сечений.

Функции resetFor3d(), resetFor2d() сбрасывают значения переменных на дефолтные для рисования в
одном  
из режимов с учётом ориентации устройства.

## Матрица преобразований

Класс матрицы преобразований не реализован. Рекомендуется использовать матрицу <code>  
float[16]</code> и функции из пакета <code>Android.OpenGL.Matrix.</code>  
Для создания единичной матрицы используется <code>resetTranslateMatrix(float[16])</code> (можно  
передать существующий массив чтобы не выделять новую память), для применения - <code>applyMatrix(  
float[16]);</code>

## Polygon

Класс для отображения прямоугольной текстуры в любом месте и положении 3д пространства (можно  
использовать в 2д режиме, но для этого есть класс SimplePolygon).  
Конструктор на вход принимает:

+ `Function<List<Object>, PImage> redrawFunction` - функция, которая будет вызвана автоматически  
  каждый раз когда будет требоваться перерисовка изображения полигона;
+ `bool saveMemory` - не держать изображение текстуры в памяти android;
+ `int paramSize` - длина списка параметров в функции перерисовки (можно изменить с помощью  
  newParamsSize);
+ класс создателя;

`void prepareAndDraw(Point a, Point b, Point c)` - отрисовка по 3м точкам.

`prepareAndDraw(Point a, Point b, float texx, float texy, float teexa, float texb)`

` public void prepareAndDraw(PVector a, PVector b, PVector c, float texx, float texy, float teexa, float texb)` -
отрисовка с учетом текстурных координат

`void setRedrawNeeded` - перед началом следующего цикла отрисовки вызвать (или нет) функцию  
перерисовки (по умолчанию вызывается при перезаходе в приложение и после создания объекта полигона)

`void delete()` - освобождает память от Bitmap текстуры и отложенно удаляет текстуру из видео  
памяти (её удалит движок при переключении страницы)

`void redrawNow()` - вызов перерисовки в ручную

## SimplePolygon

Наследуется от класса Polygon, предназначен для 2д рисования.  
Конструктор аналогичный.

`void prepareAndDraw(float rot, float x, float y, float a, float b, float z)` - отрисовывает на  
прямоугольинке, левый верхний угол в положении x,y, размеры a,b, потом поворачивает прямоугольник
на  
r радиан по часовой. z - высота над плоскостью z=0. **Идеально для рисования танчика в 2д.**
Раньше  
это был метод класса танка.

`void prepareAndDraw(float x, float y, float b, float z)` - квадрат стороной b c левым верхниим  
углом в x,y

## Shape

Класс для работы с 3д мешами. Позволяет добавлять меш, карту нормалей и текстуру. Меш хранится в  
виде массива чисел (массива объектов Face) и в виде vbo+vao в видеопамяти. **Вся работа с видео  
памятью автоматизирована**, в том числе подгрузка карты нормалей и вершин.

`Shape(String fileName, String textureFileName, GamePageInterface page)` - конструктор с
асинхронной  
подгрузкой файла вершин и синхронной (в основном потоке) загрузкой текстуры  
В основе загрузчика вершин лежит сторонняя библиотека, а текстура загружается методом  
переопределения redrawFunction.  
Принимаются только триангулированные .obj файлы. При экспорте из blender внимательно следите за  
положиенем осей и совпадением их с игровым пространством.  
Пока вершины не загружены, вызовы рисования shape не дадут никакого резуьтата.

`void addNormalMap(String normalMapFileName)` - загрузка карты нормалей. При этом соответствующая  
шейдерная переменная (вызов `getNormalMapEnableLocation()` у адаптора) будет установлена в
единицу (  
иначе 0).  
**В дефолтном lighting шейдере не происходит отключения перехода в касательное пространство при  
отключении карты нормалей.**

`void prepareAndDraw()` - отрисовать шейп с учетом текущих настроек камеры, шейдеров и матрицы  
преобразования. В случае необходимости вызывает функции перерисовки и перезагрузки вершин.

**Настоятельно не рекомендуется в ручную вызывать функции перерисовки у данного класса** (кроме  
случаев крайней необходимости).

# Анимации и SealObject

Для работы со встроенными анимациями нужно испольозовать класс SelObject. Это аналоги игрового  
объекта в юнити - обертка над мешем, от которого нужно наследовать все игровые объекты. Хотя, для  
отрисвоки просто мешей его использование не обязательно.

## класс Animantor

### добавление анимации

`addAnimation(sealObject target, Function<Animation, float[]> tf, float[] args, Function<float[], Float> vf, float duration, float vfa, long st, boolean recurring)`

Первый аргумент — это экземпляр EnObject, который является объектом анимации. Второй аргумент —
это  
функция, которая принимает экземпляр Animation и возвращает массив из 6 чисел с плавающей точкой,  
Первые 3 - это положение, вторые 3 определяют вращение (Заметьте, что это не дельты, не разность  
положений, это новые координаты). Третий аргумент - это функция, которая определяет скорость  
воздействия на атрибуты (закон их изменеия), она принимает массив содержащее значение от 0 до 1 (
0 —  
начало анимации, 1 — самый последний момент) и некоторого аргумента, функция также должна
возвращать  
значение от 0 до 1, как было упомянуто ранее 0 - это первая позиция анимации, 1 - самая последняя.  
Затем идет длительность, функция скорости и начальное время. Последний аргумент позволяет
закциклить  
анимацию (объект будет стартовать из начальных координат).  
Можно добавлять несколько анимаций, они будут выполняться параллельно, их эффекты будут  
накладываться.  
Анмации буду оставнавливаться при вызове freezeMillis() так как используют pageMillis() в качестве  
источника времени.

Пример вызова:

```  
Animator.addAnimation( this, (Animator.Animation animation) -> {
  float[] attrs = animation.getAttrs();            
  float[] args = animation.getArgs(); 
  return attrs;        
},       
new float[3],        
(float[] f) -> { 
  float k = f[0]; 
  float a = f[1];
  return f[0];
},1000,1.0f,5000);
   ```  
  
В движке есть встроенные функции анимаций, чтобы страдания не были слишком сильными.  
  
*Разработчик данного кода оставил лишь намеки на то, как работает его код, так что приведенная ниже  
документация - это скорее теория, предположения о том, как оно должно работать*  
  
### остановка анимаций  
  
`freezeAnimations(sealObject target)` ставит на паузу все анимации у данного тюленя.  
`unfreezeAnimations(sealObject target)` продолжает исполнение анимации.  
  
## SealObject  
  
`sealObject(Shape shape)` создание нового тюленя на основе существующего меша.  
  
`float[] getSpaceAttrs()` получить массив координат. Первые 3 числа - координаты, вторые - углы  
поворота вокруг этих осей.  
  
`setSpaceAttrs(float [6])` установить координаты объекта  
  
*все анимации при включении зацикливания будут возвращаться в исходную точку при начале новой  
итерации*  
  
`animMotion(float x, float y, float z, float duration, long startTiming, boolean recurring)`  
добавить движение из текущей точки вдоль вектора x y z.  
  
`animRotation(float x, float y, float z, float duration, long startTiming, boolean recurring)`  
поворот к на x, y, z вокруг одноимённых осей.  
  
`animPivotRotation(float x, float y, float z, float vx, float vy, float vz, float duration, long startTiming, boolean recurring)`  
поворот вокруг осей относительно пивота.  
  
`stopAnimations()`  заморозка всех анимаций.  
  
`continueAnimations()` отморозка всех анимаций.  
  
`setObjScale(float n)` увеличение объекта в n раз.  
  
`prepareAndDraw()` отрисовка объекта и применение анимации.  
  
## Face  
  
Служебный класс. Когда я его писал, как он работает понимали только я и Господь. Теперь это понимает  
только Господь.  
Занимается хранением данных в формате треугольного полигона и выдачей их по требованию в  
соответствующих форматах для простоты использования в адапторах.  
Написание документации на методы данного класса оставляется читателю в качестве самостоятельного  
несложного упражнения и не несёт практической пользы, так как они нужны исключительно для работы  
дефолтных шейдеров.  
В списке Face Shape хранит свои данные о вершинах (а также текстурных координатах, нормалях и тп).  
  
  
# MipMaps  

 **Opengl поддерживает только квадратные mipMaps**  
 
 Инструмент фильтрации отнесенных на большое от камеры расстояние изображений.  
 Досутпен в класса Polygon и Simplepolygon.  
 Для его использования нужно после объекта GamePageClass в параметрах конструктора дописать  true (тогда при каждой перезагрузке текстуры будут генерироваться mipMaps).  
 

# SectionPolygon  
  
Класс рисовалка линии. С помощью 1 экземпляра можно рисовать сколько угодно линий.  
`SectionPolygon(GamePageClass)` - конструктор. Класс владелец не null  
`void setColor(PVector color)` - задает цвет по rgb от 0 до 1  
  
**Рисовать только с применением шейдера линии!**  
`draw(Sectionsection)` - рисует линию (поддерживается 3д)  
  
### как создать сам шейдер линии:  
  
`new Shader(R.raw.Section_vertex, R.raw.Section_fragmant, gamePageClass, new SectionShaderAdaptor());`  
  
# Axes  
  
класс-рисовальщик осей для отладки. Внутри себя **уже содержит шейдер линии**.  
` void drawAxes(float limit, float step, float tickSize, float[] matrix, Camera camera)` -  
отрисовать систему координат с преобразованием matrix (matrix = null равносильно единичному  
преобразованию) на камеру camera. Оси рисовать от -limit до limit, на них будут отметки с шагом step  
длиной tickSize * 2. Отметки будут в обоих перепендикулярных каждой оси направлениях.  
Каждая ось обозначена цветом в положительном направлении и тем же, только более темным цветом в  
отрицательном направлении.  
  
| ось | цвет в положительном нарпавлении | цвет в отрицательном нарпалвении |  
|-----|----------------------------------|----------------------------------|  
| x   | красный                          | темно-красный                    |  
| y   | зеленый                          | темно-зеленый                    |  
| z   | синий                            | темно-синий                      |  
  
  
## SkyBox  
  
Наследуется от Shape (Добработанный класс). **В версии 3.0.x** Для своей работы требует наличия в  
assets cube.obj.  
  
`SkyBox(String textureFileName, String res, GamePageInterface page)` - первым параметром указывается  
папка, в которой лежат соответсвующие фрагменты кубической карты. Например, для случая:  
  
    skybox-
      -left.jpg 
      -right.jpg
      -top.jpg
      -bottom.jpg
      -front.jpg
      -back.jpg  
вызвать `ShyBox("skybox/","jpg",this);`  
  
`void prepareAndDraw()` - вызввать только после применения шейдеров и повторного примения после  
этого матриц камеры и проекции  (и, при необходимости, обновления других переменных). Рисует sky  
box.  
В качестве дефолтного шейдера можно использовать skybox_fragment и skybox_vertex.  
  
# Буферы и хранение данных в видео памяти  
  
Для всех рассмотренных ниже объектов работа с памятью автоматизирована, но следует учитывать, что  
некоторые объекты (напрмер, FrameBuffer) не могут хранить информацию дольше 1 кадра из-за специфики  
используеомого API.  
  
## FrameBuffer  
  
Название говорит само за себя. Предназначен для внеэкранного реднеринга в текстуру.  
  
`FrameBuffer(int width, int height, GamePageInterface page)` - Конструктор. Создает и настраивает  
объект. Принимает на вход размеры текстуры.  
  
`frameBuffer.apply()` - подключает FB. При повторном подключении буфера можно мотерять предыдущие  
результаты рендеринга. Аналогично с экранным буфером.  
  
`static void FrameBuffer.connectDefaultFrameBuffer()` - отключает внеэкранный рендеринг.  
  
изменение или получение текущих размеров буфера (не будет применено до следующего вызова onRedraw(),  
автоматизированного или ручного. Вообще, проще удалить и создать новый буфер):  
  
    public void setH(int h)    public void setW(int w)   
    public int getWidth()   
    public int getHeight()  
  
`public int getFrameBuffer()` - получить id FrameBuffer  
  
`public int getDepth()` - получить id буфера глубины  
  
`public int getTexture()` - получить id текстуры.  
  
`delete()` - очистка видеопамяти.  
  
`drawTexture(Point a, Point b, Point d)` - рисует текстуру на полигоне по 3 точкам (см класс  
Polygon).  
  
# Обработка изображений  
  
## PImage  
  
Во многом повторяет процессинг, реализует множество функций обработки bitmap. Рендеринг производит  
на процессоре, сильно бъет по памяти и производительности, но в большинстве случаев это единственный  
выход.  
  
`PImage(float x, float y)` - принимает размеры картинки в пикселях, создает объект PImage и битмап в  
нем  
  
`void delete()` - удаление битмап, но не самого объекта картинки. **Обязательно вызывать перед тем,  
как планируется удалить картинку.**  
  
Подробно с функциями рисования можно ознакомиться в исходном коде, там нет каких-либо особенностей.  
По умолчанию битмап с поддержкой альфа канала.  
  
`Utils.LoadImage(String path)` - возвращает загруженный из assets редактируемый PImage. Загрузка в  
потоке, из которого вызвана функция  
  
# Свет  
  
В основе лежит реализация с learn opengl. Созданы классы источников света, по набору переменных  
дублирующие структуры этих источников в дефолтном шейдере освещения.  
Логично, что все источники света наследуются от ShaderData.  
Когда нужно, объекты света используют вызовы записи в uniform переменную,адреса которой они получают  
напрямую из шейдера. Вызов getLocations() инициируется классом ShaderData.  
  
Все источники имеют конструктор, принимающий GamePageInterface, причем смысла делать объекты  
статическими я не вижу. Настройки параметров источников происходят путем сохранения нужных значений  
в переменные и затем вызова forwardData() ** строго перед отрисовкой и строго после применения  
шейдера. **  
  
**для использования света нужно использовать fragment_shader_light и vertex_shader_light**  
  
Все значиения цвета - от 0 до 1, все координаты - в игровом пространстве.  
  
## AmbientLight - класс фонового света  
  
* `PVector color` - цвет рассеянного света.  
  
## DirectedLight - класс направленного света от плоского или бесконечно удаленного источника.  
  
* `PVector color` - цвет источника  
  
* `PVector direction` - направление источника.  
  
* `float diffuse` - компонента рассечнного света от источника (яркость)  
  
* `float specular` - компонента блика от источника (интенсивность)  
  
## PointLight - класс точечного источника  
  
Светит во все стороны, интенсивоность быстро падает с расстоянием  
  
* `PVector color` - цвет источника  
  
* `PVector position` - положение в игровом пространстве  
  
* `float diffuse` - вклад компоненты рассеянного освещения от этого источника (яркость).  
  
* `float specular` - вклад компоненты с бликом.  
  
* `float constant, Sectionar, quadratic` - коэффициенты убывания вклада с расстоянием. Вклад считается  
  по формуле 1.0 / (light.constant + light.Sectionar * distance + light.quadratic * (distance *  
  distance))  
  
## SourceLight - класс фонаря.  
  
Фактически это точечный источник, но светящий в указанном направлении внутри конуса с указанным  
углом раствора.  
Обладает теми дже полями, что и PointLight, а также:  
  
* ` cutOff, outerCutOff` - телесные углы в градусах начала и конца дияракционного затухания света на  
  границе пучка. Ругелирует плавность границы пучка.  
  
## Material  
  
На данном этапе поддержки карты материалов нет. Свойства матриала постоянны в рамках меша.  
  
Все PVector компоненты повзоляют добиваться отттенков цветов матриала при рендеринге света путем  
скалярного произведения на соответствующие компоненты света.  
  
```  

    public PVector ambient;  - компонента рассеянного света
    public PVector diffuse;  - компонента диффузии падающго света
    public PVector specular;  - компонента блика
    public float shininess;  - компонента яркости блика
    
```  

## Экспозиция

Достигается методом пост-обработки. Для этого есть встроенный шейдер exposition fragment, который  
используется вместе с vertex_shader:

```  
    expositonShader = new Shader(com.example.gl_engine.R.raw.vertex_shader, 
      com.example.gl_engine.R.raw.exposition_fragment,
      this, new MainShaderAdaptor());
```  
  
Содержимое сцены реднерится во фрейм буфер, далее подключется шейдер экспозиции, с помощью  
экземпеляра класса `ExpouseSettings` в него передаются значения и вызывается отрисовка.  
  
```  
expouseSettings.expouse = expouse.value;
expouseSettings.gamma = gamma.value;
```  

# Обработка касаний

**до версии 3.1.0 использовались вызовы touchStarted, touchMoved и touchEnded, которые были
удалены  
в связи со сложностями работы в режиме мультитчача**

С версии 3.1.0 основным инструментом является TouchProcessor.

## класс TouchProcessor

Данный класс является конструктором, содержащим в себе

* обработчик хитбокса
* вызов touchStarted
* вызов touchMoved
* вызов TouchEnded
* расстановку приоритетов обработки касаний.

  **не нужно хранить экземпляры этих классов в своих переменных, если их можно конвертировать в  
  локальные значения. Экземпляры и так хранятся в памяти движка**  
  Ниже приведен пример использования TouchProcessor:


```  
  //*************************************************//  
  new TouchProcessor(this::touchProcHitbox, this::touchStartedCallback,  				
  	this::touchMovedCallback, this::touchEndCallback, this);  
  //*************************************************//      
  private Boolean touchProcHitbox(TouchPoint event) {  
            return true если так попал в нужных хитбокс, иначе false;
  }  
  private Void touchStartedCallback(TouchPoint p) {  
       //выполнить действия            
       return null;     
  }        
  private Void touchMovedCallback(TouchPoint p) {  
       //выполнить действия            
       return null;     
  }         
  private Void touchEndCallback(TouchPoint t) {  
            //выполнить действия            
            return null;     
  }  
  ```  

Функции можно объявить и лямбда функциями или с другими именами.  
**Все, кроме первого параметра, могут быть null**  
Последний параметр - страница-родитель, **не рекомендуется ставить null.**  
Класс TouchPoint имеет следующие публичные поля и содержит информацию о последне обработанном мете  
нахождения касания:  
`public float touchX, touchY` - координаты в пикселях

## правила обработки касаний

* В случае пересечения заданных в touchProcHitbox (первый параметр коннструктора), выше приоритет
  у  
  того класса, который был создан раньше во время исполнения (исключение - дебаггер при его  
  использовании, он обладает высшим приоритетом)
* При появлении нового касания, происходит проверка всех хитбоксов в порядке приоритета
* При срабатывании одного из них, вызывается touchStartedCallback (второй параметр), происходит  
  захват тача
* Дальше конкретный палец привязывается к конкретному объекту. Он будет отслеживаться пока не  
  оторвется от экрана или не вызовут terminate() у данного объекта
* В любой момент вызовом terminate() можно незамедлительно прервать отслеживание и инициировать  
  вызов touchEnded()
* **настоятельно рекомендуется сразу после terminate() вставить return при вызове из коллбека во  
  избежание сложностей, так как вызов terminate() не завершает выполнение коллбека, но под капотом  
  является вызовом touchEndedCallback)**
* При прекращении отслеживаия пальца через terminate() вызывается touchEndedCallback(null), при  
  исчезновении пальца на экране - от последнего известного TouchPoint
* Последний известный TouchPoint является публичным полем экземпляра touchProcesor,может быть null
* Хитбокс не может сработать, если объект уже отслеживает палец. Если нужно отследить несколько  
  касаний, начавшихся в одном хитбоксе - нужно создать несколько разных объектов с одинаковыми  
  параметрами конструктора
* TouchProcessor с не null последниим параметром удаляются при уходе со страницы
* TouchProcessor может быть деактивирован методом block() и зново активирован методом unblock().  
  Рекомендуется вызывать после terminate или вне обработчиков.
* long getDuration() - возвращает время удержания пальца на экране в миллисекундах или -1 (если в  
  данный момент палец не отслеживается)
* boolean getTouchAlive() - true если объект отслеживает палец, иначе false

# Пакет maths

На данный момент находится на стадии разработки

## Vec3 и PVector

**два класса решают одну и ту же задачу преобразования над векторами. PVector ведет себя как в
процесинге, если метод не статик, то копирование вектора не происходит и изменяются его данные.
Статические методы делают копирование. У Vec3 почти нет статических методов, все операции над
векторами по умолчанию выполняют копирование.**

Доступны конструкторы:

```  
public Vec3(float x, float y, float z) {    
    this.x = x;    
    this.y = y;    
    this.z = z;  
}    
    
public Vec3() {    
    this.x = 0;    
    this.y = 0;    
    this.z = 0;  
}    
    
// Creates vector with values taken from give array. Reads 3 values, starting from i index. 
public Vec3(float[] arr, int i) {    
    x = arr[i];    
    y = arr[i + 1];    
    z = arr[i + 2];  
}    
    
public Vec3(Vec3 v) {    
    this.x = v.x;    
    this.y = v.y;    
    this.z = v.z;  
}    
    
public Vec3(float v) {    
    this.x = v;    
    this.y = v;    
    this.z = v;  
}    
    
public Vec3(float x, float y) {    
    this.x = x;    
    this.y = y;  
}  
```  

Также через конструкторы можно конвертировать PVector в Vec3 и назад.
**Все эти конструкторы и методы доступны и для PVector.**

### выгрузка данных

Получить вектор в виде массива длины 3:  
` public float[] getArray() `

### преобразования над векторами

`public void normalize()` - нормировать

`public float length()` - длина по теореме Пифагора

`public Vec3 add(Vec3 v)` - прибавить 2 вектора

`public Vec3 sub(Vec3 v)` - вычесть из 1го второй (из this в случае метода )

`public Vec3 mul(float i)` - скалярное умножение

`public void cross(Vec3 u)` - векторное произведение

`public Vec3 div(float i)` - скалярное деление

`public static float getAngle(Vec3 v, Vec3 u)` - угол в радианах между векторами  
` public static float dot(Vec3 a, Vec3 b)` - скалярное произведение

```  
/**    
     * rotates vector around axis for a specified angle     *       
     * @param axis axis, around which to rotate    
     * @param a    angle in degrees    
     */    public void rotateVec3(Vec3 axis, float a) {    
        //create empty translate matrix    
        float[] matrix;    
        matrix = resetTranslateMatrix(new float[16]);    
        Matrix.rotateM(matrix, 0, a, axis.x, axis.y, axis.z);    
        float[] resultVec = new float[4];    
        Matrix.multiplyMV(resultVec, 0, matrix, 0, new float[]{this.x, this.y, this.z, 0}, 0);    
        //return new Vec3(resultVec[0], resultVec[1], resultVec[2]);    
        this.x = resultVec[0];    
        this.y = resultVec[1];    
        this.z = resultVec[2];    
}      
}  
  ```
## Section 
  
`Section(Vec3 A, Vec3 B)` - поздание отрезка из точки А и точку Б  
`public Vec3 getDirectionVector() ` - возвращает вектор из точки Б а точку А  
`public Vec3 getBaseVector()` - возвращает точку А  
`public Vec3 findCross(Sectionn)` - вернет точку пересечения 2х отрезков или null, если они не  
пересекаются  
  
# Utils  
  
Содежрит множество постоянно дорабатываемх функций, решаюших простейшие задачи. Самые полезные:  
  
## адаптация  
  
`kx, ky` - поля, инициализуемые движком при запуске. kx = размер экрана по горизонтали в  
пикселях/720; ky = размер экрана по вертикали в пикселях/1280. Коэффициенты для атаптации  
интерфейса.  
  
`x, y` - размер экрана по горизонтали и вертикали в пикселях.  
  
## работа со времененм  
  
`millis()` - время работы приложения с момента последнего запуска в миллисекундах с учетом  
остановок.  
  
`freezeMillis()` - останавливает ход millis и pageMillis. При этом частота вызовов отрисовки не  
меняется. Останавливет работу всех объектов анимаций.  
  
`unfreezeMillis()` - millis и pageMillis продолжают идти с того же места, на котором были  
остановлены. Продолжает работу всех объектов анимаций.  
  
`absoluteMillis()` - время работы приложения с момента последнего запуска в миллисекундах без учета  
остановок.  
  
`getMillisFrozen()` - true елси ход millis остановлен, иначе false.  
  
Для любой ориентации устройсвта:  
  
`float getTimeK()` - 120/текущие фпс - коэффициент для адаптации физики к реальному времени. **Равен  
0 при заморозке millis**.  
  
## Также полезные поля других классов  
  
`float OpenGLRenderer.fps` - текущее значение фпс  
  
`OpenGLRenderer.pageMillis()` - время в миллисекундах с момента открытия текущей страницы. При  
заморозке millis также замораживается.  
  
# Debugger  
  
Класс позволяет на лету, без останоки исполнения с помощью графического интерфейса просмаотривать и  
изменять значения переменных  
**отрисовка интерфейса не оптимизирована, не используйте этот механимз в продакшене и не пугайтесь  
просадкам фпс**  
Для использования дебагера измеените третий параметр строки  
`GLSurfaceView v = engine.onCreate(this, unused -> new MainRenderer(), false,false); //второй параметр - ориентация LandScape, третий - использовать ли встроенный дебаггер.`  
в MainActivity на true.  
В левом верхнем углу появится табличка с фпс.  
Для добавления переменных нужно создать несколько (сколько угодно) экземпляров переменных для  
отладки  
  
## Отладка  
  
Каждой переменной соотносится объект класса DebugValueFloat. Другие типы данных пока не  
поддерживаются.  
У класса есть публичное поле value, с которым следует обращаться как с обычной переменной. Его можно  
свободно читать и свободно туда писать, все изменнеия мометально отобрзятся в пользовательском  
интерфейсе. Все вносимые пользователем изменения в переменную моментально попадают в value.  
  
Для создания переменной используйте метод класса Debugger  
`DebugValueFloat Debugger.addDebugValueFloat(float min, float max, @NotNull String name)`, где min и  
max - границы, в которых пользователь может изменять значения переменной, name - отображаемое в  
интерфейсе имя переменной. **нельзя создавать 2 переменные с одинаковым именем, попытка сделать это  
не даст никакого эффекта и будет проигнорирвана**.  
  
## использование  
  
Окно отладчика открывается нажатием на поле с fps в левом верхнем углу, закрывается либо повтороным  
нажатием в то же место, либо с помощью креста внизу по центру.  
Стрелки --> и <-- позволяют перемещаться по списку переменных, если они не поместились на 1 экран.  
**При выключенном режиме отладки отладчик не инициализован и не занимает память. При вклюении он  
занимает некоторую доп. память для отрисовки интерфейса, а также имеет наивысший приоритет обработки  
касаний. Если окно свернуто, то наивысший приоритет только у таблички с фпс.**  
  
При развернутом окне отладчика в правом верхнем углу отображаетс версия движка, в левом верхнем -  
текущие фпс.  
  
Окно отладчика намеренно сделано полупрозрачным, *это не баг, а фича.*  
  
При нажатии на переменную появится возможность отрегулировать ее значение с помощью слайдера,  
вернуться в главное меняю можно с помощью кнопки под слайдером.  
  

  
