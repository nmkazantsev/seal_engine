# SealEngine

## Что это? Зачем оно надо?
Данный движок приван:
 1. Там, где это возможно, избавить пользователя от вызовов низкоуровневых и неинтуитивных методов API OpenGL.
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
        GLSurfaceView v = engine.onCreate(this, unused -> new MainRenderer(), false,false); //второй параметр - ориентация LandScape, третий - использовать ли встроенный дебаггер.
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
Движок поддерживает как создание классической пары vertex+fragment:

    shader = new Shader(com.example.gl_engine.R.raw.vertex_shader,com.example.gl_engine.R.raw.fragment_shader, this, new MainShaderAdaptor());//create default example shader
так и использование геометрического шейдера:

    shader = new Shader(vertex_shader, geom_shader, fragment_shader, this, new MainShaderAdaptor());

Шейдеры рекомендуется объявлять final:

    private final Shader shader;

Чтобы применить шейдер:

	applyShader(shader);//static method of class ShaderUtils

Логи о компиляции будут выводиться с тегом Info. Отсутсвие логов - признак успешной компиляции.
При примении шейдера все объекты, унаследованные от ShaderData автоматически прогрузят свои значения туда (исключение - Material).
В движке в стандартной папке ресурсов есть дефолтные шейдеры для рендеринга, для света и для работы со skyBox.

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

## Класс ShaderData
Это абстрактный класс, при реализации значения, находящегося в видео памяти (uniform переменная) нужно от нгео унаследоваться и переопределить

`void getLocations(int programId)` - загрузка id переменной из шейдера (на вход поступает id скомпилированной связки шейдеров)

`void forwardData()` - отправка данных в шейдер

`void delete()` - очистка видеопамяти
Функции вызываются движком при применении шейдера или смене страницы. 

При применении вызываются getLocstions, при смене страницы - delete.
Смысла удалять uniform переменную нет, но есть смысл удалять ссылки на объект из массивов, например, удалить объект света из массива источников для данной страницы.

Если ссылка на переменную 0, то ничего не произойдёт. Если на переменную ссылаются 2 ссылки, то будет записано второе значение, первое перезапишется.

# Преобразования вершин и инструменты работы с ними

## Camera
Класс игровой камеры. Объединяет в себе CameraSettings и ProjectionMatrixSettings (которые вяляются его полями). 

Конструктор сразу настраивает камеру для работы с 3д, вызывая соответствующие методы у обоих полей класса.

`void apply()` - применние настроек камеры, вызывать перед рисованием объектов. Если перед этим была вызвана resetFor3d(), то перспектива будет включена, если перед этим была вызвана restFor2d() - выключена.

`apply(boolean perspectiveEnabled)` - ручное управление перспективой.

**настройками полей класса можно управлять по отдельности, но в большинстве случаев так делать не рекомендуется**

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

`void setRedrawNeeded` - перед началом следующего цикла отрисовки вызвать (или нет) функцию перерисовки (по умолчанию вызывается при перезаходе в приложение и после создания объекта полигона)

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

`void prepareAndDraw()` - отрисовать шейп с учетом текущих настроек камеры, шейдеров и матрицы преобразования. В случае необходимости вызывает функции перерисовки и перезагрузки вершин. 

**Настоятельно не рекомендуется в ручную вызывать функции перерисовки у данного класса** (кроме случаев крайней необходимости).



## Face
Служебный класс. Когда я его писал, как он работает понимали только я и Господь. Теперь это понимает только Господь.
Занимается хранением данных в формате треугольного полигона и выдачей их по требованию в соответствующих форматах для простоты использования в адапторах.
Написание документации на методы данного класса оставляется читателю в качестве самостоятельного несложного упражнения и не несёт практической пользы, так как они нужны исключительно для работы дефолтных шейдеров. 
В списке Face Shape хранит свои данные о вершинах (а также текстурных координатах, нормалях и тп).


## SkyBox
Наследуется от Shape (Добработанный класс). **В версии 3.0.x** Для своей работы требует наличия в assets cube.obj.

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

# Обработка изображений
## PImage
Во многом повторяет процессинг, реализует множество функций обработки bitmap. Рендеринг производит на процессоре, сильно бъет по памяти и производительности, но в большинстве случаев это единственный выход.

`PImage(float x, float y)` - принимает размеры картинки в пикселях, создает объект PImage и битмап в нем

`void delete()` - удаление битмап, но не самого объекта картинки. **Обязательно вызывать перед тем, как планируется удалить картинку.**

Подробно с функциями рисования можно ознакомиться в исходном коде, там нет каких-либо особенностей. По умолчанию битмап с поддержкой альфа канала.

`Utils.LoadImage(String path)` - возвращает загруженный из assets редактируемый PImage. Загрузка в потоке, из которого вызвана функция

# Свет
В основе лежит реализация с learn opengl. Созданы классы источников света, по набору переменных дублирующие структуры эттх источников в дефолтном шейдере освещения. 
Логично, что все источники света наследуются от ShaderData.
Кодна нужно, объекты света используют вызовы записи в uniform переменную,адреса которой они получают напрямую из шейдера. Вызов getLocations() инициируется классом ShaderData.

Все источники имеют конструктор, принимающий GamePageInterface, причем смысла делать объекты статическими я не вижу. Настройки параметров источников происходят путем сохранения нужных значений в переменные и затем вызова forwardData() ** строго перед отрисовкой и строго после применения шейдера. **

**для использования света нужно использовать fragment_shader_light и vertex_shader_light**

Все значиения цвета - от 0 до 1, все координаты - в игровом пространстве.
## AmbientLight - класс фонового света
* `Vec3 color` - цвет рассеянного света.

## DirectedLight - класс направленного света от плоского или бесконечно удаленного источника.

* `Vec3 color` - цвет источника

* `Vec3 direction` - направление источника.

* `float diffuse` - компонента рассечнного света от источника (яркость)

* `float specular` - компонента блика от источника (интенсивность)

## PointLight - класс точечного источника
Светит во все стороны, интенсивоность быстро падает с расстоянием

* `Vec3 color` - цвет источника

* `Vec3 position` - положение в игровом пространстве

* `float diffuse` - вклад компоненты рассеянного освещения от этого источника (яркость).

* `float specular` - вклад компоненты с бликом.

* `float constant, linear, quadratic` - коэффициенты убывания вклада с расстоянием. Вклад считается по формуле  1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance))

## SourceLight - класс фонаря.
Фактически это точечный источник, но светящий в указанном направлении внутри конуса с указанным углом раствора.
Обладает теми дже полями, что и PointLight, а также:

* ` cutOff, outerCutOff` - телесные углы в градусах начала и конца дияракционного затухания света на границе пучка. Ругелирует плавность границы пучка.

## Material
На данном этапе поддержки карты материалов нет. Свойства матриала постоянны в рамках меша.

Все vec3 компоненты повзоляют добиваться отттенков цветов матриала при рендеринге света путем скалярного произведения на соответствующие компоненты света.
```
    public Vec3 ambient;  - компонента рассеянного света
    public Vec3 diffuse;  - компонента диффузии падающго света
    public Vec3 specular;  - компонента блика
    public float shininess;  - компонента яркости блика
```



# Обработка касаний
**до версии 3.1.0 использовались вызовы touchStarted, touchMoved и touchEnded, которые были удалены в связи со сложностями работы в режиме мультитчача**

С версии 3.1.0 основным инструментом является TouchProcessor.
## класс TouchProcessor
Данный класс является конструктором, содержащим в себе
* обработчик хитбокса
* вызов touchStarted
* вызов touchMoved
* вызов TouchEnded
* расстановку приоритетов обработки касаний.

  **не нужно хранить экземпляры этих классов в своих переменных, если их можно конвертировать в локальные значения. Экземпляры и так хранятся в памяти движка**
  Ниже приведен пример использования TouchProcessor:


  ```
  //*************************************************//
  new TouchProcessor(this::touchProcHitbox, this::touchStartedCallback,
  this::touchMovedCallback, this::touchEndCallback, this);
  //*************************************************//
  
	private Boolean touchProcHitbox(TouchPoint event) {
	        return true если так попал в нужных хитбокс, иначк false;
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
   Класс TouchPoint имеет следующие публичные поля и содержит информацию о последне обработанном мете нахождения касания:
  `public float touchX, touchY` - координаты в пикселях

## правила обработки касаний
* В случае пересечения заданных в touchProcHitbox (первый параметр коннструктора), выше приоритет у того класса, который был создан позже во время исполнения (исключение - дебаггер при его использовании, он обладает высшим приоритетом)
* При появлении нового касания, происходит проверка всех хитбоксов в порядке приоритета
* При срабатывании одного из них, вызывается touchStartedCallback (второй параметр), происходит захват тача
* Дальше конкретный палец привязывается к конкретному объекту. Он будет отслеживаться пока не оторвется от экрана или не вызовут terminate() у данного объекта
* В любой момент вызовом terminate() можно незамедлительно прервать отслеживание и инициировать вызов touchEnded()
* **настоятельно рекомендуется сразу после terminate() вставить return при вызове из коллбека во избежание сложностей, так как вызов terminate() не завершает выполнение коллбека, но под капотом является вызовом touchEndedCallback)**
* При прекращении отслеживаия пальца через terminate() вызывается touchEndedCallback(null), при исчезновении пальца на экране - от последнего известного TouchPoint
* Последний известный TouchPoint является публичным полем экземпляра touchProcesor,может быть null
* Хитбокс не может сработать, если объект уже отслеживает палец. Если нужно отследить несколько касаний, начавшихся в одном хитбоксе - нужно создать несколько разных объектов с одинаковыми параметрами конструктора
* TouchProcessor с не null последниим параметром удаляются при уходе со страницы
* TouchProcessor может быть деактивирован методом block() и зново активирован методом unblock(). Рекомендуется вызывать после terminate или вне обработчиков.
* long getDuration() - возвращает время удержания пальца на экране в миллисекундах или -1 (если в данный момент палец не отслеживается)
* boolean getTouchAlive() - true если объект отслеживает палец, иначе false

  
# Пакет maths
На данный момент находится на стадии разработки

# Utils
Содежрит множество постоянно дорабатываемх функций, решаюших простейшие задачи. Самые полезные:

## адаптация
`kx, ky` - поля, инициализуемые движком при запуске. kx = размер экрана по горизонтали в пикселях/720; ky = размер экрана по вертикали в пикселях/1280. Коэффициенты для атаптации интерфейса.

`x, y` - размер экрана по горизонтали и вертикали в пикселях.

## работа со времененм

`millis()` - время работы приложения с момента последнего запуска в миллисекундах с учетом остановок.

`freezeMillis()` - останавливает ход millis и pageMillis. При этом частота вызовов отрисовки не меняется.

`unfreezeMillis()` - millis и pageMillis продолжают идти с того же места, на котором были остановлены.

`absoluteMillis()` -  время работы приложения с момента последнего запуска в миллисекундах без учета остановок.

`getMillisFrozen()` - true елси ход millis остановлен, иначе false.

Для любой ориентации устройсвта:

`float getTimeK()` - 120/текущие фпс - коэффициент для адаптации физики к реальному времени. **Равен 0 при заморозке millis**.

## Также полезные поля других классов

`float OpenGLRenderer.fps` - текущее значение фпс

`OpenGLRenderer.pageMillis()` - время в миллисекундах с момента открытия текущей страницы. При заморозке millis также замораживается.


# Debugger
Класс позволяет на лету, без останоки исполнения с помощью графического интерфейса просмаотривать и изменять значения переменных
**отрисовка интерфейса не оптимизирована, не используйте этот механимз в продакшене и не пугайтесь просадкам фпс**
Для использования дебагера измеените третий параметр строки 
`GLSurfaceView v = engine.onCreate(this, unused -> new MainRenderer(), false,false); //второй параметр - ориентация LandScape, третий - использовать ли встроенный дебаггер.`
в MainActivity на true.
В левом верхнем углу появится табличка с фпс.
Для добавления переменных нужно создать несколько (сколько угодно) экземпляров переменных для отладки 
## Отладка
Каждой переменной соотносится объект класса DebugValueFloat. Другие типы данных пока не поддерживаются.
У класса есть публичное поле value, с которым следует обращаться как с обычной переменной. Его можно свободно читать и свободно туда писать, все изменнеия мометально отобрзятся в пользовательском интерфейсе. Все вносимые пользователем изменения в переменную моментально попадают в value.

Для создания переменной используйте метод класса Debugger
`DebugValueFloat Debugger.addDebugValueFloat(float min, float max, @NotNull String name)`, где min и max - границы, в которых пользователь может изменять значения переменной, name - отображаемое в интерфейсе имя переменной. **нельзя создавать 2 переменные с одинаковым именем, попытка сделать это не даст никакого эффекта и будет проигнорирвана**.

## использование
Окно отладчика открывается нажатием на поле с fps в левом верхнем углу, закрывается либо повтороным нажатием в то же место, либо с помощью креста внизу по центру.
Стрелки --> и <-- позволяют перемещаться по списку переменных, если они не поместились на 1 экран.
**При выключенном режиме отладки отладчик не инициализован и не занимает память. При вклюении он занимает некоторую доп. память для отрисовки интерфейса, а также имеет наивысший приоритет обработки касаний. Если окно свернуто, то наивысший приоритет только у таблички с фпс.**

При развернутом окне отладчика в правом верхнем углу отображаетс версия движка, в левом верхнем - текущие фпс.

Окно отладчика намеренно сделано полупрозрачным, *это не баг, а фича.*

При нажатии на переменную появится возможность отрегулировать ее значение с помощью слайдера, вернуться в главное меняю можно с помощью кнопки под слайдером.

# Примеры
## Пример простейшего класса страницы с отрисовкой 3д
```
package com.manateam.main;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.glClearColor;
import static com.seal.gl_engine.OpenGLRenderer.fps;
import static com.seal.gl_engine.OpenGLRenderer.mMatrix;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.applyMatrix;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.resetTranslateMatrix;
import static com.seal.gl_engine.engine.main.frameBuffers.FrameBufferUtils.createFrameBuffer;
import static com.seal.gl_engine.engine.main.shaders.Shader.applyShader;
import static com.seal.gl_engine.utils.Utils.cos;
import static com.seal.gl_engine.utils.Utils.map;
import static com.seal.gl_engine.utils.Utils.millis;
import static com.seal.gl_engine.utils.Utils.radians;
import static com.seal.gl_engine.utils.Utils.x;
import static com.seal.gl_engine.utils.Utils.y;

import android.opengl.GLES30;
import android.opengl.Matrix;

import com.manateam.main.redrawFunctions.MainRedrawFunctions;
import com.seal.gl_engine.GamePageClass;
import com.seal.gl_engine.OpenGLRenderer;
import com.seal.gl_engine.default_adaptors.LightShaderAdaptor;
import com.seal.gl_engine.default_adaptors.MainShaderAdaptor;
import com.seal.gl_engine.engine.main.camera.Camera;
import com.seal.gl_engine.engine.main.debugger.DebugValueFloat;
import com.seal.gl_engine.engine.main.debugger.Debugger;
import com.seal.gl_engine.engine.main.frameBuffers.FrameBuffer;
import com.seal.gl_engine.engine.main.frameBuffers.FrameBufferUtils;
import com.seal.gl_engine.engine.main.light.AmbientLight;
import com.seal.gl_engine.engine.main.light.DirectedLight;
import com.seal.gl_engine.engine.main.light.Material;
import com.seal.gl_engine.engine.main.light.SourceLight;
import com.seal.gl_engine.engine.main.shaders.Shader;
import com.seal.gl_engine.engine.main.touch.TouchProcessor;
import com.seal.gl_engine.engine.main.verticles.Poligon;
import com.seal.gl_engine.engine.main.verticles.Shape;
import com.seal.gl_engine.engine.main.verticles.SkyBox;
import com.seal.gl_engine.maths.Point;
import com.seal.gl_engine.maths.Vec3;
import com.seal.gl_engine.utils.SkyBoxShaderAdaptor;
import com.seal.gl_engine.utils.Utils;

public class SecondRenderer extends GamePageClass {
    private final Poligon fpsPoligon;
    private final Shader shader, lightShader, skyBoxShader;
    Camera camera;
    private final Shape s;
    private final SkyBox skyBox;
    private final SourceLight sourceLight;
    private final AmbientLight ambientLight;
    private final DirectedLight directedLight1;
    private final Material material;
    private FrameBuffer frameBuffer;

    TouchProcessor touchProcessor;

    DebugValueFloat camPos;

    public SecondRenderer() {
        shader = new Shader(com.example.gl_engine.R.raw.vertex_shader, com.example.gl_engine.R.raw.fragment_shader, this, new MainShaderAdaptor());
        lightShader = new Shader(com.example.gl_engine.R.raw.vertex_shader_light, com.example.gl_engine.R.raw.fragment_shader_light, this, new LightShaderAdaptor());
        fpsPoligon = new Poligon(MainRedrawFunctions::redrawFps, true, 1, this);
        camera = new Camera();
        s = new Shape("ponchik.obj", "texture.png", this);
        s.addNormalMap("noral_tex.png");

        ambientLight = new AmbientLight(this);
        // ambientLight.color = new Vec3(0.3f, 0.3f, 0.3f);

        directedLight1 = new DirectedLight(this);
        directedLight1.direction = new Vec3(-1, 0, 0);
        directedLight1.color = new Vec3(0.9f);
        directedLight1.diffuse = 0.2f;
        directedLight1.specular = 0.8f;
       /* directedLight2 = new DirectedLight(this);
        directedLight2.direction = new Vec3(0, 1, 0);
        directedLight2.color = new Vec3(0.6f);
        directedLight2.diffuse = 0.9f;
        directedLight2.specular = 0.8f;

        */
        sourceLight = new SourceLight(this);
        sourceLight.diffuse = 0.8f;
        sourceLight.specular = 0.9f;
        sourceLight.constant = 1f;
        sourceLight.linear = 0.01f;
        sourceLight.quadratic = 0.01f;
        sourceLight.color = new Vec3(0.5f);
        sourceLight.position = new Vec3(2.7f, 0, 0);
        sourceLight.direction = new Vec3(-0.3f, 0, 0);
        sourceLight.outerCutOff = cos(radians(40));
        sourceLight.cutOff = cos(radians(30f));

        material = new Material(this);
        material.ambient = new Vec3(1);
        material.specular = new Vec3(1);
        material.diffuse = new Vec3(1);
        material.shininess = 1.1f;

        skyBox = new SkyBox("skybox/", "jpg", this);
        skyBoxShader = new Shader(com.example.gl_engine.R.raw.skybox_vertex, com.example.gl_engine.R.raw.skybox_fragment, this, new SkyBoxShaderAdaptor());

        touchProcessor = new TouchProcessor(MotionEvent -> true, touchPoint -> {
            OpenGLRenderer.startNewPage(new MainRenderer());
            return null;
        }, null, null, this);
        frameBuffer = createFrameBuffer((int) x, (int) y, this);

        camPos = Debugger.addDebugValueFloat(2, 5, "cam pos");
        camPos.value = 4;
    }


    @Override
    public void draw() {
        GLES30.glDisable(GL_BLEND);
        FrameBufferUtils.connectFrameBuffer(frameBuffer.getFrameBuffer());
        camera.resetFor3d();
        camera.cameraSettings.eyeZ = 0f;
        camera.cameraSettings.eyeX = camPos.value;
        float x = 3.5f * Utils.sin(millis() / 1000.0f);
        camera.cameraSettings.centerY = 0;
        camera.cameraSettings.centerZ = x;
        applyShader(skyBoxShader);
        camera.apply();
        skyBox.prepareAndDraw();
        applyShader(lightShader);
        material.apply();
        glClearColor(1f, 1, 1, 1);
        camera.apply();
        mMatrix = resetTranslateMatrix(mMatrix);
        Matrix.rotateM(mMatrix, 0, map(millis() % 10000, 0, 10000, 0, 360), 1, 0.5f, 0);
        Matrix.translateM(mMatrix, 0, 0, -0f, 0);
        Matrix.scaleM(mMatrix, 0, 0.5f, 0.5f, 0.55f);
        applyMatrix(mMatrix);
        s.prepareAndDraw();
        FrameBufferUtils.connectDefaultFrameBuffer();

        applyShader(shader);
        fpsPoligon.setRedrawNeeded(true);
        camera.resetFor2d();
        camera.apply();
        mMatrix = resetTranslateMatrix(mMatrix);
        applyMatrix(mMatrix);
        fpsPoligon.redrawParams.set(0, String.valueOf(fps));
        fpsPoligon.redrawNow();
        //  fpsPoligon.prepareAndDraw(new Point(0 * kx, 0, 1), new Point(100 * kx, 0, 1), new Point(0 * kx, 100 * ky, 1));
        frameBuffer.drawTexture(new Point(Utils.x, Utils.y, 1), new Point(0, y, 1), new Point(Utils.x, 0, 1));
    }
}


```

## пример работы в режиме 2д и использования класса анимаций
```
package com.manateam.main;

import static android.opengl.GLES20.glClearColor;
import static com.seal.gl_engine.OpenGLRenderer.mMatrix;
import static com.seal.gl_engine.OpenGLRenderer.pageMillis;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.applyMatrix;
import static com.seal.gl_engine.engine.main.frameBuffers.FrameBufferUtils.connectDefaultFrameBuffer;
import static com.seal.gl_engine.engine.main.frameBuffers.FrameBufferUtils.connectFrameBuffer;
import static com.seal.gl_engine.engine.main.frameBuffers.FrameBufferUtils.createFrameBuffer;
import static com.seal.gl_engine.engine.main.shaders.Shader.applyShader;
import static com.seal.gl_engine.utils.Utils.kx;
import static com.seal.gl_engine.utils.Utils.ky;
import static com.seal.gl_engine.utils.Utils.x;
import static com.seal.gl_engine.utils.Utils.y;

import com.manateam.main.redrawFunctions.MainRedrawFunctions;
import com.seal.gl_engine.GamePageClass;
import com.seal.gl_engine.OpenGLRenderer;
import com.seal.gl_engine.default_adaptors.MainShaderAdaptor;
import com.seal.gl_engine.engine.main.animator.Animator;
import com.seal.gl_engine.engine.main.camera.Camera;
import com.seal.gl_engine.engine.main.engine_object.sealObject;
import com.seal.gl_engine.engine.main.frameBuffers.FrameBuffer;
import com.seal.gl_engine.engine.main.shaders.Shader;
import com.seal.gl_engine.engine.main.touch.TouchPoint;
import com.seal.gl_engine.engine.main.touch.TouchProcessor;
import com.seal.gl_engine.engine.main.verticles.Poligon;
import com.seal.gl_engine.engine.main.verticles.Shape;
import com.seal.gl_engine.engine.main.verticles.SimplePoligon;
import com.seal.gl_engine.maths.Point;
import com.seal.gl_engine.utils.Utils;

public class MainRenderer extends GamePageClass {
    private final Poligon polygon;
    private final Shader shader;
    private final Camera camera;
    private static SimplePoligon simplePolygon;
    private final sealObject s;
    boolean f = true;
    private final TouchProcessor touchProcessor;
    private final FrameBuffer frameBuffer;

    public MainRenderer() {
        Animator.initialize();
        shader = new Shader(com.example.gl_engine.R.raw.vertex_shader, com.example.gl_engine.R.raw.fragment_shader, this, new MainShaderAdaptor());
        polygon = new Poligon(MainRedrawFunctions::redrawFps, true, 0, this);
        polygon.redrawNow();
        camera = new Camera();
        if (simplePolygon == null) {
            simplePolygon = new SimplePoligon(MainRedrawFunctions::redrawBox2, true, 0, null);
            simplePolygon.redrawNow();
        }

        touchProcessor = new TouchProcessor(this::touchProcHitbox, this::touchStartedCallback, this::touchMovedCallback, this::touchEndCallback, this);
        TouchProcessor touchProcessor2 = new TouchProcessor(MotionEvent -> true, this::touchStartedCallback, this::touchMovedCallback, this::touchEndCallback, this);

        s = new sealObject(new Shape("building_big.obj", "box.jpg", this));
        s.setObjScale(0.2f);
        s.animMotion(1f, 0f, -6f, 1000, 1000, false);
        s.animRotation(0f, 0f, 90f, 3000, 1000, false);
        s.animRotation(90f, 0, 0, 1000, 3000, false);
        s.animMotion(1f, 0, 0, 500, 6000, true);
        TouchProcessor touchProcessor = new TouchProcessor(this::touchProcHitbox, this::touchStartedCallback, this::touchMovedCallback, this::touchEndCallback, this);
        frameBuffer = createFrameBuffer((int) x, (int) y, this);
    }


    @Override
    public void draw() {
        if (f && pageMillis() >= 500) {
            s.stopAnimations();
            f = false;
        }
        if (pageMillis() >= 1500) s.continueAnimations();
        applyShader(shader);
        glClearColor(1f, 1f, 1f, 1);
        camera.resetFor3d();
        camera.cameraSettings.eyeZ = 5;
        camera.apply();
        connectFrameBuffer(frameBuffer.getFrameBuffer());
        s.prepareAndDraw();
        connectDefaultFrameBuffer();
        camera.resetFor2d();
        camera.apply(false);
        applyMatrix(mMatrix);
        polygon.prepareAndDraw(new Point(110 * kx, 0, 1), new Point(200 * kx, 0, 1), new Point(110 * kx, 100 * ky, 1));
        if (touchProcessor.getTouchAlive()) {
            simplePolygon.prepareAndDraw(0, touchProcessor.lastTouchPoint.touchX, touchProcessor.lastTouchPoint.touchY, 300, 300, 0.01f);
        }
        frameBuffer.drawTexture(new Point(Utils.x, Utils.y, 1), new Point(0, y, 1), new Point(Utils.x, 0, 1));

    }

    private Boolean touchProcHitbox(TouchPoint event) {
        return event.touchX < x / 2;
    }

    private Void touchStartedCallback(TouchPoint p) {
        return null;
    }

    private Void touchMovedCallback(TouchPoint p) {
        return null;
    }

    private Void touchEndCallback(TouchPoint t) {
        OpenGLRenderer.startNewPage(new SecondRenderer());//запуск страницы только если тач начался в нужном хитбоксе
        return null;
    }
}


```







