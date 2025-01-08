package com.clase.buscaminas_manuel;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * @author Manuel
 * @version  1.0*/

public class MainActivity extends AppCompatActivity implements Instrucciones.OnRespuesta{
    // Declaramos todas las variables necesarias y las inicializamos si hace falta
    private Partida partida = null; // Una instancia de la partida
    private GridLayout tableroGrid = null; // El tablero
    private SharedPreferences sharedPreferences = null; // Los sharedPreferences para guardar la bomba favorita
    private int imagenBomba = R.drawable.bomba; // La bomba predefinida
    private ImageView bomba = null; // La imagen de la bomba
    private Toolbar tol = null; // El toolbar
    private int minas = 10; // para la partida predefinida le damos este valor
    private int filas = 8; // para la partida predefinida le damos este valor
    private int columnas = 8; // para la partida predefinida le damos este valor
    private MediaPlayer sonidoClick = null; // Para el sonido de clickar simple una casilla
    private MediaPlayer sonidoBoing = null; // Para el sonido de mantener click largo en una casilla
    private boolean conSonido = true; // Para reproducir o no el sonido

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Establecemos el toolbar como el action bar y obtenemos su componente
        tol = findViewById(R.id.toolbar);
        setSupportActionBar(tol);

        // Obtenemos el grid que creamos anteriormente en la vista
        tableroGrid = findViewById(R.id.tableroGridLayout);

        // Obtenemos los sonidos de los botones
        sonidoClick = MediaPlayer.create(MainActivity.this, R.raw.click);
        sonidoBoing = MediaPlayer.create(MainActivity.this, R.raw.boing);

        // Declaramos las imagen de la bomba y le ponemos un evento para cuando la clickemos
        bomba = (ImageView) findViewById(R.id.imgBomba);
        bomba.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                elegirBomba(); // Este evento nos llevará a un método para elegir la bomba que queremos
            }
        });

        // Cargamos los sharedPreferences y en caso establecemos la bombaElegida por defecto si no hay ningun dato guardado
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        imagenBomba = sharedPreferences.getInt("bombaElegida", R.drawable.bomba);
        // También al cargar el sharedPreferences, lo que hacemos es cargar si queremos jugar con audio o no, en caso de que no haya ningun dato si que jugamos con sonido
        conSonido = sharedPreferences.getBoolean("confiSonido", true);
        // Ponemos la imagen de la bomba el recurso cargado, si no hay ninguno, es predefinido
        bomba.setImageResource(imagenBomba);

        // Iniciamos la partida predefinida que es un nivel facil de 8x8 con 10 minas
        iniciarPartidaPredefinida(filas, columnas, minas);
    }

    /**
     * @param newConfig
     * Método que nos permite agregar unas configuraciones al haber cambios en la app, como puede
     * ser rotar la pantalla u otro posible, pero en esta app yo solo lo utilizo para ajustar
     * los botones al hacer la rotación de pantalla*/
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Cuando hay un cambio en la configuración, procedemos a llamar al método de ajustar el tamaño de los botones
        ajustarTamañoBotones();
    }

    /**
     * @param outState
     * Método @override que nos sirve para guardar todos los datos de la partida antes de destruir la actividad, ya
     * seá por cambiar la orientación de la pantalla o por algún error*/
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Guardar el estado del tablero y el tamaño del grid
        outState.putSerializable("tablero", partida.getTablero()); // Guardamos el tablero
        outState.putBoolean("juegoAcabado", partida.isJuegoAcabado()); // Guardamos si el juego ha acabdo o no
        outState.putInt("minas", partida.getMinas()); // Guardamos el número de minas
        outState.putInt("filas", filas);    // Guardar el número de filas
        outState.putInt("columnas", columnas); // Guardar el número de columnas
        tableroGrid = findViewById(R.id.tableroGridLayout); // Guardamos el layout
    }

    /**
     * @param savedInstanceState
     * Este método es una especie de complemento del anterior, en este lo que hacemos precisamente, es una vez
     * que la actividad anterior ha sido destruida o eliminada y se ha cargado una nueva, cargar los datos que
     * nos guardamos anteriormente para en este caso poder seguir la partida del buscaminas y que no ocurran errores*/
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Obtenemos el tablero grid en el que ponemos la matriz de casilla
        tableroGrid = findViewById(R.id.tableroGridLayout);

        // Comprobamos si tenemos algun estado de instancia guardado, es decir, si no es nulo, en caso de si tenerlo
        if (savedInstanceState != null) {
            // Creamos una nueva instancia de tablero y obtenemos el dato tablero que guardamos anteriormente
            Tablero tableroRestaurado = (Tablero) savedInstanceState.getSerializable("tablero");
            // Obtenemos si el juego esta acabado o no
            boolean juegoAcabado = savedInstanceState.getBoolean("juegoAcabado");
            // Obtenemos el número de minas de la partida
            int minas = savedInstanceState.getInt("minas");

            // Aquí procedemos a por así decirlo cargar los datos a tablero, establecerle el tablero, si está acaba
            // la partida y las minas que se han colocado
            partida.setTablero(tableroRestaurado);
            partida.setJuegoAcabado(juegoAcabado);
            partida.setMinas(minas);

            // Obtenemos el número de filas y columnas del nuevo tablero
            int filas = partida.getTablero().getFilas();
            int columnas = partida.getTablero().getColumnas();

            // Establecemos las variables globales como las filas, columnas y minas que hemos obtenido, si no hacemos esto
            // pueden surgir errores del estilo que aunque tengamos una matriz 12x12 si reiniciamos el juego o perdemos
            // y volvemos a jugar se nos configura un nivel simple
            this.filas = filas;
            this.columnas = columnas;
            this.minas = minas;

            // Llamamos al métodod e crear Tablero en donde como ya explicare procedemos a crear el grid de casillas
            // y agregar sus funcionalidades a cada una
            crearTablero(filas, columnas);

            // Aquí compruebo si el juego está acabado o no
            if(partida.isJuegoAcabado()){ // Si esta acabado
                // Descubrimos tosas las casillas con el método ya creado y hacemos otra comprobación
                partida.getTablero().descubrirTodasLasCasillasCuandoPierde(tableroGrid, imagenBomba);
                if(partida.comprobarVictoria()){ // Si resulta que el juego está acabado y hemos ganado
                    mostrarDialogoFinGanado(); // Mostramos el dialogo de final ganado
                }else{ // Si esta acabado pero no ganado
                    mostrarDialogoFinPerdido(); // Mostramos el dialogo de final perdido
                }
            }else{ // Si el juego no esta acabado
                // Actualizar la interfaz para reflejar el estado actual
                actualizarInterfazDeUsuario(filas, columnas);
            }
        }
    }

    /**
     * @param filas
     * @param columnas
     * Método paraa ctualizar las interfaz una vez girada la pantalla y restablecido la partida y el tablero
     * guardado anteriormente*/
    private void actualizarInterfazDeUsuario(int filas, int columnas) {
        // Obtenemos el tablero o grid de nuestra pantalla
        tableroGrid = findViewById(R.id.tableroGridLayout);
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                // Creamos por así decirlo una instancia de una casilla con las coordenadas indicadas
                Casilla casilla = partida.getTablero().getCasilla(i, j);
                // Creamos el botón de esa posición también
                Button boton = (Button) tableroGrid.getChildAt(i * columnas + j);

                // Comprobamos si la casilla está descubierta o no
                if (casilla.isCasillaDescubierta()) { // En caso de que este descubierta
                    // Comprobamos si la casilla tiene una mina o no la tiene
                    if (casilla.tieneMina()) { // Si la casilla tiene mina
                        boton.setBackgroundResource(imagenBomba); // Establecemos el fondo con la imagen de la bomba
                    } else { // Si la casilla no tiene mina
                        if(casilla.getNumero()>0) { // En el caso de que esa casi tenga alguna mina alrededor
                            boton.setText(String.valueOf(casilla.getNumero())); // Mostrar número de minas cercanas
                        }
                    }
                    // Ademnas ponemos el botón como no habilitado para que así no pueda ser vuelto a clickar
                    boton.setEnabled(false);
                    boton.setBackgroundColor(Color.RED); // Cambiar el color de fondo
                    boton.setTextColor(Color.WHITE); // Poner el color del texto en blanco
                }else if(casilla.isCasillaMarcada()){ // Si la casilla está marcada como posible mina
                    boton.setText("\uD83D\uDEA9"); // Le establecemos que está marcada
                    boton.setEnabled(false);
                } else { // Si no está descubierta y no está marcada
                    boton.setText(""); // Limpiamos el texto por si ha habido algún error
                    boton.setEnabled(true); // Habilitamos el botón para que se pueda clickar
                }
            }
        }
    }

    /**
     * Método para ajustr el tamaño de los botones en caso de que se necesite, ya que se redimensione
     * la pantalla o se gire, lo del displayMetrics estube buscando y también es aplicable
     * https://developer.android.com/reference/android/util/DisplayMetrics*/
    private void ajustarTamañoBotones() {
        // Obtener las dimensiones de la pantalla
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int anchoPantalla = metrics.widthPixels;

        // Calculo el tamaño del botón basandome en las columnas existentes
        int dimensionBoton = anchoPantalla / columnas;

        // Recorrer todos los botones y ajusto su tamaño
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                Button boton = (Button) tableroGrid.getChildAt(i * columnas + j); // Leo cada boton
                ViewGroup.LayoutParams params = boton.getLayoutParams(); // le establezco unos parametros
                params.width = dimensionBoton;  // Establecezco el ancho dinámicamente
                params.height = dimensionBoton; // Establecer el alto dinámicamente
                boton.setLayoutParams(params); // Confirmamos y establecemos los los parametros a cada botón
            }
        }
    }

    /**
     * Método para la creación e inflacción del menu bar
     * @param menu
     * @return */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflar el menú y añadirlo a la barra de acción (ActionBar)
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * @param item
     * @return
     * Método para jugar con la casuistica del menu y sus opciones*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId(); // Obtenemos el item seleccionado
        if (id == R.id.action_instructions) { // En caso de que sean las instrucciones
            Instrucciones instru = new Instrucciones();
            instru.show(getSupportFragmentManager(), "Instrucciones");
            return true;
        } else if (id == R.id.action_restart_game) { // En caso de que seá reiniciar el juego
            iniciarPartidaPredefinida(filas,columnas,minas);
            Toast.makeText(MainActivity.this, "Juego Reiniciado!!!", Toast.LENGTH_LONG).show();
            return true;
        } else if (id == R.id.action_settings) { // En caso de que seá elegir el nivel de dificultad
            elegirNivel();
            return true;
        }else if (id == R.id.audio_settings) { // En caso de que seá preferencias de audio
            configurarAudio();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Método para configurar la elección del sonido*/
    private void configurarAudio(){
        // Inflamos la vista del dialogo para mostrarlo
        View eleccion = LayoutInflater.from(MainActivity.this).inflate(R.layout.efectos_especiales, null);
        // Creamos el constructor necesarios y le ponemos nuestra vista
        AlertDialog.Builder eleccionDialogo = new AlertDialog.Builder(MainActivity.this);
        eleccionDialogo.setView(eleccion);

        // Obtengo el spinner
        Spinner audio = (Spinner) eleccion.findViewById(R.id.spinnerSonido);

        // Creo y configuro el adaptador para este spinner en concreto
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sonido_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        audio.setAdapter(adapter);

        // Configuro la elección de audio del spinner
        audio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Obtengo el valor de la elección del spinner
                String tipoAudio = adapterView.getItemAtPosition(i).toString();
                Log.d("SpinnerSelection", "Elemento seleccionado: " + tipoAudio);
                // Filtramos la elección según la respuesta
                if (tipoAudio.equals("Áctivado")) { // En caso de que seá áctivado
                    conSonido = true; // Establecemos la variable en true
                } else if (tipoAudio.equals("Desactivado")) { // En caso de que seá desactivado
                    conSonido = false; // Establecemos la variable en false
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Creamos el dialogo a partir del constructor
        final AlertDialog dialogo = eleccionDialogo.create();
        // Establecemos el fondo como transparente para que se siga viendo el tablero y resto de la pantalla
        dialogo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogo.setCancelable(false);
        dialogo.show(); // Mostramos el dialogo

        // Obtengo el botón de dentro del layout para aceptar el sonido
        Button aceptarSonido = (Button) eleccion.findViewById(R.id.btnConfiSonido);
        // Le asigno un evento para cuando hagamos click
        aceptarSonido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Guardar la preferencia de sonido en SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("confiSonido", conSonido);
                editor.apply();
                dialogo.dismiss(); // Cerramos el dialogo
            }
        });
    }

    /**
     * Método usado para la elección del nivel de dificultad, donde tenemos diferentes posibilidad
     * como puede ser el nivel facil que es un 8x8 con 10 minas, el nivel medio que es un 12x12 con 30 minas
     * o el nivel dificil que es un 16x16 con 60 minas*/
    private void elegirNivel() {
        // Inflamos la vista del dialogo para mostrarlo
        View eleccion = LayoutInflater.from(MainActivity.this).inflate(R.layout.elegir_nivel, null);
        // Creamos el constructor necesarios y le ponemos nuestra vista
        AlertDialog.Builder eleccionDialogo = new AlertDialog.Builder(MainActivity.this);
        eleccionDialogo.setView(eleccion);

        // Obtengo el spinner de este layout
        Spinner nivel = (Spinner) eleccion.findViewById(R.id.spinner_nivel);

        // Configuro el adaptador para este spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.levels_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nivel.setAdapter(adapter);

        // Configuramos el sistema de elección de dificultad del jeugo
        nivel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Obtengo la elección del spinner
                String tipoNivel = adapterView.getItemAtPosition(i).toString();
                //Log.d("SpinnerSelection", "Elemento seleccionado: " + tipoNivel);
                // Vamos comprobando que tipo de nivel o dificultad han elegido
                if (tipoNivel.equals("Fácil")) { // En caso de que seá facil
                    // Establecemos las medidas e iniciamos la partida
                    filas = 8;
                    columnas = 8;
                    minas = 10;
                    iniciarPartidaPredefinida(filas, columnas, minas);
                } else if (tipoNivel.equals("Medio")) { // En caso de que seá medio
                    // Establecemos las medidas e iniciamos la partida
                    filas = 12;
                    columnas = 12;
                    minas = 30;
                    iniciarPartidaPredefinida(filas, columnas, minas);
                } else if (tipoNivel.equals("Difícil")) { // En caso de que seá dificil
                    // Establecemos las medidas e iniciamos la partida
                    filas = 16;
                    columnas = 16;
                    minas = 60;
                    iniciarPartidaPredefinida(filas, columnas, minas);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Creamos el dialogo a partir del constructor
        final AlertDialog dialogo = eleccionDialogo.create();
        // Establecemos el fondo como transparente para que se siga viendo el tablero y resto de la pantalla
        dialogo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogo.setCancelable(false);
        dialogo.show(); // Mostramos el dialogo

        // Obtenemos el botón de aceptar el nivel
        Button aceptarNivel = (Button) eleccion.findViewById(R.id.btnElegiNivel);
        // Configuramos el evento de cuando le clickamos
        aceptarNivel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogo.dismiss(); // Cerramos el dialogo
            }
        });
    }

    /**
     * Método el cual utilizo para poner abrir la vista de elección de la bomba deseada, en este método,
     * muestro la vista o layout, configuro el spinner y su adaptador y barajo todas las posibilidades
     * de elección en el mismo
     * */
    private void elegirBomba(){
        // Inflamos la vista del dialogo para mostrarlo
        View elegirBomba = LayoutInflater.from(MainActivity.this).inflate(R.layout.elegir_bomba, null);
        // Creamos el constructor necesarios y le ponemos nuestra vista
        AlertDialog.Builder eleccionDialogo = new AlertDialog.Builder(MainActivity.this);
        eleccionDialogo.setView(elegirBomba);

        // Creamos el dialogo a partir del constructor
        final AlertDialog dialogo = eleccionDialogo.create();
        // Le establecemos el fondo como transparente para que siga viendo el tablero
        dialogo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogo.setCancelable(false);
        dialogo.show(); // Y mostramos el dialogo

        // Obtenemos el spinner deseado dentro de la vista/layout de elegirBomba
        Spinner spinner = elegirBomba.findViewById(R.id.spinnerBomba);

        // También obtengo la imagen de la bomba de esta actividad para posteriormente cambiarla
        ImageView bom = (ImageView) findViewById(R.id.imgBomba);

        // Configuramos el sistema de elección de la bomba
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String tipoBomba = adapterView.getItemAtPosition(i).toString(); // Obtenemos el tipo de bomba elegida
                if(tipoBomba.equals("Bomba Clásica")){ // En caso de que seá bomba clásica
                    bom.setImageResource(R.drawable.bomba); //Establezco la imagen con la bomba que elegí
                    imagenBomba = R.drawable.bomba; // Y guardo en la variable global la elegida
                }else if(tipoBomba.equals("Torpedo")){ // En caso de que seá la de torpedo
                    bom.setImageResource(R.drawable.bomber);
                    imagenBomba = R.drawable.bomber;
                }else if(tipoBomba.equals("Mina Submarina")){ // En caso de que seá la mina submarina
                    bom.setImageResource(R.drawable.mina_naval);
                    imagenBomba = R.drawable.mina_naval;
                }else if(tipoBomba.equals("Granada")){ // En caso de que seá la granada
                    bom.setImageResource(R.drawable.granada);
                    imagenBomba = R.drawable.granada;
                }else if(tipoBomba.equals("Dinamita")){ // En caso de que seá la dinamita
                    bom.setImageResource(R.drawable.dinamita);
                    imagenBomba = R.drawable.dinamita;
                }else if(tipoBomba.equals("Coctel Molotov")){ // En caso de que seá el coctel molotv
                    bom.setImageResource(R.drawable.botella);
                    imagenBomba = R.drawable.botella;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Creo un array de string en donde voy a ir enlazando la posición del array bombas_array que es como se ponen lo nombres
        // en el spinner y además le voy a agregar el tipo de bomba y su archivo de imagen a cada una, como para que vayan
        // unidas
        String[] items = getResources().getStringArray(R.array.bombas_array);
        int[] icons = {
                R.drawable.bomba,
                R.drawable.bomber,
                R.drawable.mina_naval,
                R.drawable.granada,
                R.drawable.dinamita,
                R.drawable.botella
        };

        // Inicio Nuevo
        // Recuperar selección previa desde SharedPreferences
        int imagenSeleccionada = sharedPreferences.getInt("bombaElegida", R.drawable.bomba); // Predeterminado a Bomba Clásica
        int posicionSeleccionada = 0;

        // Buscar la posición de la imagen en el array de íconos
        for (int i = 0; i < icons.length; i++) {
            if (icons[i] == imagenSeleccionada) {
                posicionSeleccionada = i;
                break;
            }
        }
        // Declaro y creo el adaptador para el spinner de la bomba
        BombaAdapter adapter = new BombaAdapter(this, R.layout.item_bomba, R.id.item_text, items, icons);
        // Establezco el adaptador al spinner
        spinner.setAdapter(adapter);
        // Establecer la posición previamente seleccionada
        spinner.setSelection(posicionSeleccionada);

        // Obtenemos el botón elegir del layout de elegirBomba
        Button elegir = (Button) elegirBomba.findViewById(R.id.btnElegirBomba);
        // Le asignamos un evento que sucede cuando le clickamos
        elegir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Guardar la bomba elegida en SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("bombaElegida", imagenBomba);
                editor.apply();
                dialogo.dismiss(); // Cerramos el dialogo
            }
        });
    }

    /**
     * @param s
     * Método para devolver el okay de los dialogos*/
    @Override
    public void onRespuesta(String s) {
        Toast.makeText(getApplicationContext(), s,Toast.LENGTH_LONG).show();
    }

    /**
     * @param columnas
     * @param filas
     * @param minas
     * Método para iniciar una partida, en un principio cuando iniciemos la app, se inicializará con una matriz
     * 8x8 y 10 minas que es el nivel fácil y el que tenemos como predefinido*/
    private void iniciarPartidaPredefinida(int filas, int columnas, int minas){
        partida = new Partida(filas, columnas, minas); // Creamos una nueva partida pasandole las filas, columnas y minas
        partida.emepzarPartida(); // Empezados la partida
        crearTablero(filas, columnas); // Y creamos el tablero
    }

    /**
     * @param filas
     * @param columnas
     * Método principal encargado de la creación del tablero, sus botones y acciones sobre ellos*/
    private void crearTablero(int filas, int columnas) {
        // Limpiamos todas las vistas anteriores del grid si esque tiene
        tableroGrid.removeAllViews();
        // Establecemos una vez borradas las anteriores vistas las filas y columnas
        tableroGrid.setRowCount(filas);
        tableroGrid.setColumnCount(columnas);
        // Definimos el margen para los botones
        int margen = 3;

        // Procedemos a crear el tablero de casilals y configurar cada botón
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                // Creamos un botón justo en una posición determinada como puede ser 0,0 0,1 0,2
                Button boton = new Button(getApplicationContext());
                // Le ponemos el padding menor posible para que así el si ponemos el nivel dificil que son muchas casillas
                // y giramos la pantalla para que se vea lo maximo posible el número o la bomba
                boton.setPadding(1, 1, 1, 1);
                // Creamos las variables x e y y les atribuimos el valor de i y j
                int x = i;
                int y = j;
                boton.setTag(new Pair<>(i, j)); // Tag con la posición (fila, columna)

                // Configurar LayoutParams para cada botón
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;  // Distribución igualitaria en ancho
                params.height = 0; // Distribución igualitaria en alto
                params.rowSpec = GridLayout.spec(i, 1f);
                params.columnSpec = GridLayout.spec(j, 1f);
                params.setMargins(margen, margen, margen, margen); // Establecemos el margen
                // Establecemos a los botones los parametros que anteriormente configuramos
                boton.setLayoutParams(params);

                // Le agregamos un evento al boton que si por lo que sea le clicamos llame al metodo onCasillaClick
                boton.setOnClickListener(view -> {
                    onCasillaClick(view);
                });

                // Le agregamos un evento al boton que si por lo que sea le hacemos un click largo
                boton.setOnLongClickListener(view -> {
                    // Comprobación para saber su jugamos con sonido o sin el
                    if(conSonido==true){ // Si jugamos con sonido
                        sonidoBoing.start(); // Reproducimos el sonido establecido al tocar una casilla
                    }
                    // Tenemos que confirmar que la mina que hemos marcado como posible mina sea de verdad una
                    // para ello en partida obtenemos la casilla y llamamos a su método para saber si tiene mina o ni
                    // si no tiene lo que hacemos en mostrar el dialogo de final perdido y devolvemos true para acabar con la intereacion
                    if (!partida.getCasilla(x,y).tieneMina()) {
                        partida.setJuegoAcabado(true);
                        partida.getTablero().descubrirTodasLasCasillasCuandoPierde(tableroGrid, imagenBomba);
                        mostrarDialogoFinPerdido();
                        Toast.makeText(MainActivity.this, "Eso no erá un mina", Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    // Si por lo que seá hemos acertado en que es una mina, proseguimos con las comprobaciones
                    // Tenemos que tener en cuenta que podemos marcar y desmarcar, no tiene mucho sentido ya que solo podemos
                    // marcar las minas
                    boton.setText("\uD83D\uDEA9");
                    boton.setEnabled(false);
                    Toast.makeText(MainActivity.this, "Mina localizada!!", Toast.LENGTH_SHORT).show();

                    // Actualizamos si la casilla ha sido marcada o no
                    partida.marcarCasilla(x, y);

                    // En cada click largo comprobamos si cumple la condición del método de comprobar victoria
                    if (partida.comprobarVictoria()) {
                        // Si la cumple, mostramos el dialogo de final ganado
                        mostrarDialogoFinGanado();
                    }
                    // Devolvemos true para confirmar que el click largo ha salido bien
                    return true;
                });

                // Ahora una vez configurado el boton y todas sus funcionalidades procedemos a agregarle a la vista del grid
                tableroGrid.addView(boton);
            }
        }
    }

    /**
     * @param view
     * Método de la casillaClickSimple para desarrollar toda la lógica de cuando
     * tocamos una casilla*/
    public void onCasillaClick(View view) {
        // Comprobación para saber su jugamos con sonido o sin el
        if(conSonido==true){ // Si jugamos con sonido
            sonidoClick.start(); // Reproducimos el sonido establecido al tocar una casilla
        }
        // Obtener la posición (fila, columna) desde el Tag del botón
        Pair<Integer, Integer> posicion = (Pair<Integer, Integer>) view.getTag();
        int fila = posicion.first;
        int columna = posicion.second;
        // Comrpobamos si podemos seguir descubriendo casillas, es decir el método de la condición devuelve un boolean
        // si este es falso significa que alguno de los filtros del método no ha pasado, entonces si no lo pasa
        // significa que o el juego está acabado o que hemos perdido
        if (!partida.descubrirCasilla(fila, columna, tableroGrid)) {
            // Entonces si pasa eso, significa que la partida ha acabado por lo cuál descubrimos todas las casillas
            partida.getTablero().descubrirTodasLasCasillasCuandoPierde(tableroGrid, imagenBomba);
            // Y por último mostramos el dialogo de final perdido
            mostrarDialogoFinPerdido();
        }
    }

    /**
     * Método en donde abrimos el dialogo de que se ha acabado el juego y ademas hemos perdido*/
    public void mostrarDialogoFinPerdido(){
        // Inflamos la vista del dialogo para mostrarlo
        View perdido = LayoutInflater.from(this).inflate(R.layout.final_perdido, null);
        // Creamos el constructor necesarios y le ponemos nuestra vista
        AlertDialog.Builder eleccionDialogo = new AlertDialog.Builder(this);
        eleccionDialogo.setView(perdido);

        // Creamos el dialogo a partir del constructor
        final AlertDialog dialogo = eleccionDialogo.create();
        // Le establecemos el fondo como un fondo transparente
        dialogo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogo.setCancelable(false);
        dialogo.show(); // Mostramos el dialogo

        // Declaramos el boton de aceptar
        Button aceptar = (Button) perdido.findViewById(R.id.btnAceptar);
        // Le ponemos un evento al botón al ser clicado
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iniciarPartidaPredefinida(filas,columnas,minas); // Iniciamos una nueva partida
                dialogo.dismiss(); // Ocultamos el dialogo
            }
        });
    }

    /**
     * Método que nos sirve para mostrar el dialogo de que se ha acabado el juego y has ganado*/
    public void mostrarDialogoFinGanado(){
        // Inflamos la vista del dialogo para mostrarlo
        View ganado = LayoutInflater.from(this).inflate(R.layout.final_ganado, null);
        // Creamos el constructor necesarios y le ponemos nuestra vista
        AlertDialog.Builder eleccionDialogo = new AlertDialog.Builder(this);
        eleccionDialogo.setView(ganado);

        // Creamos el dialogo a partir del constructor
        final AlertDialog dialogo = eleccionDialogo.create();
        // Le establecemos el fondo como un fondo transparente
        dialogo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogo.setCancelable(false); // Establecemos que no se pueda cerrar tocando otro sitio
        dialogo.show(); // Mostramos el dialogo

        // Declaramos el boton de aceptar
        Button aceptar = (Button) ganado.findViewById(R.id.btnAceptar);
        // Le ponemos un evento al botón al ser clicado
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iniciarPartidaPredefinida(filas,columnas,minas); // Iniciamos una nueva partida
                dialogo.dismiss(); // Ocultamos el dialogo
            }
        });
    }
}