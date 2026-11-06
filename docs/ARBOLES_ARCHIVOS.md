# Árboles de archivos

Generados desde los archivos que se preparan para versionar. Se excluyen compilaciones, configuración de la computadora, credenciales, datos personales y dependencias instaladas.

## Código de producción

```text
AgendaPersonal/
└── app/
    └── src/
        └── main/
            └── java/
                └── com/
                    └── example/
                        └── agendapersonal/
                            ├── AgendaApplication.java
                            ├── MainActivity.java
                            ├── avisos/
                            │   ├── HoraAviso.java
                            │   ├── ProgramadorAvisos.java
                            │   ├── ReceptorAvisos.java
                            │   └── VentanaAvisos.java
                            ├── data/
                            │   ├── DatabaseHelper.java
                            │   └── ImportadorHistorico.java
                            ├── eventos/
                            │   ├── CambiosEvento.java
                            │   ├── ReglaRecurrencia.java
                            │   └── SeriesEvento.java
                            ├── model/
                            │   ├── AmbitoEmocional.java
                            │   ├── ClasificacionEconomia.java
                            │   ├── ClasificacionSocial.java
                            │   ├── Direccion.java
                            │   ├── Ejercicio.java
                            │   ├── Especialidad.java
                            │   ├── Estudio.java
                            │   ├── Evento.java
                            │   ├── FrasePersonal.java
                            │   ├── Freelance.java
                            │   ├── Gasto.java
                            │   ├── HistorialPostergacion.java
                            │   ├── Ingreso.java
                            │   ├── Materia.java
                            │   ├── MotivoEmocional.java
                            │   ├── ObservacionRI.java
                            │   ├── Pago.java
                            │   ├── Persona.java
                            │   ├── RegistroEmocional.java
                            │   ├── RegistroInvestigacion.java
                            │   ├── Salud.java
                            │   ├── SerieEvento.java
                            │   ├── Social.java
                            │   ├── SyncChange.java
                            │   ├── SyncDevice.java
                            │   ├── SyncReceipt.java
                            │   ├── Tarea.java
                            │   └── Trabajo.java
                            ├── repository/
                            │   ├── AmbitoEmocionalRepository.java
                            │   ├── ClasificacionEconomiaRepository.java
                            │   ├── ClasificacionSocialRepository.java
                            │   ├── DireccionRepository.java
                            │   ├── EjercicioRepository.java
                            │   ├── EspecialidadRepository.java
                            │   ├── EstudioRepository.java
                            │   ├── EventoRepository.java
                            │   ├── FrasePersonalRepository.java
                            │   ├── FreelanceRepository.java
                            │   ├── GastoRepository.java
                            │   ├── HistorialPostergacionRepository.java
                            │   ├── IngresoRepository.java
                            │   ├── MateriaRepository.java
                            │   ├── MotivoEmocionalRepository.java
                            │   ├── ObservacionRIRepository.java
                            │   ├── PagoRepository.java
                            │   ├── PersonaRepository.java
                            │   ├── RegistroEmocionalRepository.java
                            │   ├── RegistroInvestigacionRepository.java
                            │   ├── SaludRepository.java
                            │   ├── SerieEventoRepository.java
                            │   ├── SocialRepository.java
                            │   ├── SyncChangeRepository.java
                            │   ├── SyncDeviceRepository.java
                            │   ├── SyncReceiptRepository.java
                            │   ├── TareaRepository.java
                            │   └── TrabajoRepository.java
                            ├── respaldo/
                            │   ├── ArchivoRespaldo.java
                            │   ├── CifradoRespaldo.java
                            │   ├── EsquemaRespaldo.java
                            │   ├── GestorRespaldo.java
                            │   └── ReglasCuentaRespaldo.java
                            ├── sync/
                            │   ├── AlmacenSync.java
                            │   ├── CambioSync.java
                            │   ├── CoordinadorSync.java
                            │   ├── IdentidadLocal.java
                            │   ├── JsonSync.java
                            │   ├── LocalSync.java
                            │   ├── MotorSync.java
                            │   ├── ReglasEstadoSync.java
                            │   ├── ReglasSync.java
                            │   ├── SesionSegura.java
                            │   ├── SesionSupabase.java
                            │   ├── SupabaseRest.java
                            │   ├── TrabajoSync.java
                            │   └── TransporteSync.java
                            ├── ui/
                            │   ├── ActividadTematica.java
                            │   ├── AgendaActivity.java
                            │   ├── AjustesActivity.java
                            │   ├── AmbienteInicioView.java
                            │   ├── AyudaActivity.java
                            │   ├── CatalogosActivity.java
                            │   ├── ConexionActivity.java
                            │   ├── Destinos.java
                            │   ├── EconomiaActivity.java
                            │   ├── EjercicioActivity.java
                            │   ├── EstadoEmocionalActivity.java
                            │   ├── EstiloApp.java
                            │   ├── EstudiosActivity.java
                            │   ├── EventosActivity.java
                            │   ├── FrasesActivity.java
                            │   ├── FreelanceActivity.java
                            │   ├── GraficoLineasView.java
                            │   ├── Iconos.java
                            │   ├── InvestigacionesActivity.java
                            │   ├── MenuNavegacion.java
                            │   ├── MovimientosActivity.java
                            │   ├── NotificacionesActivity.java
                            │   ├── PersonasActivity.java
                            │   ├── Postergaciones.java
                            │   ├── ProgresoCircularView.java
                            │   ├── ProgresoDia.java
                            │   ├── ProgresoInicio.java
                            │   ├── RadarView.java
                            │   ├── RegistroActivity.java
                            │   ├── RespaldoActivity.java
                            │   ├── ResumenDia.java
                            │   ├── SaludActivity.java
                            │   ├── SocialesActivity.java
                            │   ├── TareasActivity.java
                            │   ├── Tema.java
                            │   ├── TrabajoActivity.java
                            │   └── registro/
                            │       ├── Campo.java
                            │       ├── Definicion.java
                            │       ├── Fuente.java
                            │       ├── Fuentes.java
                            │       └── Registro.java
                            └── util/
                                ├── Csv.java
                                ├── JsonCursorHelper.java
                                ├── ReglasAgenda.java
                                └── UuidGenerator.java
```

## Recursos, pruebas y resto del repositorio

```text
AgendaPersonal/
├── .gitattributes
├── .gitignore
├── CHANGELOG.md
├── CONTRIBUTING.md
├── README.md
├── app/
│   ├── .gitignore
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── androidTest/
│       │   └── java/
│       │       └── com/
│       │           └── example/
│       │               └── agendapersonal/
│       │                   ├── AlmacenSyncInstrumentedTest.java
│       │                   ├── ContextoPrueba.java
│       │                   ├── DiaSyncInstrumentedTest.java
│       │                   ├── ExampleInstrumentedTest.java
│       │                   ├── ModulosInstrumentedTest.java
│       │                   ├── PersistenciaSyncInstrumentedTest.java
│       │                   ├── eventos/
│       │                   │   └── RecurrenciaDosCasosInstrumentedTest.java
│       │                   └── respaldo/
│       │                       └── GestorRespaldoInstrumentedTest.java
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/
│       │   │   └── com/
│       │   │       └── example/
│       │   │           └── agendapersonal/
│       │   │               ├── AgendaApplication.java
│       │   │               ├── MainActivity.java
│       │   │               ├── avisos/
│       │   │               │   ├── HoraAviso.java
│       │   │               │   ├── ProgramadorAvisos.java
│       │   │               │   ├── ReceptorAvisos.java
│       │   │               │   └── VentanaAvisos.java
│       │   │               ├── data/
│       │   │               │   ├── DatabaseHelper.java
│       │   │               │   └── ImportadorHistorico.java
│       │   │               ├── eventos/
│       │   │               │   ├── CambiosEvento.java
│       │   │               │   ├── ReglaRecurrencia.java
│       │   │               │   └── SeriesEvento.java
│       │   │               ├── model/
│       │   │               │   ├── AmbitoEmocional.java
│       │   │               │   ├── ClasificacionEconomia.java
│       │   │               │   ├── ClasificacionSocial.java
│       │   │               │   ├── Direccion.java
│       │   │               │   ├── Ejercicio.java
│       │   │               │   ├── Especialidad.java
│       │   │               │   ├── Estudio.java
│       │   │               │   ├── Evento.java
│       │   │               │   ├── FrasePersonal.java
│       │   │               │   ├── Freelance.java
│       │   │               │   ├── Gasto.java
│       │   │               │   ├── HistorialPostergacion.java
│       │   │               │   ├── Ingreso.java
│       │   │               │   ├── Materia.java
│       │   │               │   ├── MotivoEmocional.java
│       │   │               │   ├── ObservacionRI.java
│       │   │               │   ├── Pago.java
│       │   │               │   ├── Persona.java
│       │   │               │   ├── RegistroEmocional.java
│       │   │               │   ├── RegistroInvestigacion.java
│       │   │               │   ├── Salud.java
│       │   │               │   ├── SerieEvento.java
│       │   │               │   ├── Social.java
│       │   │               │   ├── SyncChange.java
│       │   │               │   ├── SyncDevice.java
│       │   │               │   ├── SyncReceipt.java
│       │   │               │   ├── Tarea.java
│       │   │               │   └── Trabajo.java
│       │   │               ├── repository/
│       │   │               │   ├── AmbitoEmocionalRepository.java
│       │   │               │   ├── ClasificacionEconomiaRepository.java
│       │   │               │   ├── ClasificacionSocialRepository.java
│       │   │               │   ├── DireccionRepository.java
│       │   │               │   ├── EjercicioRepository.java
│       │   │               │   ├── EspecialidadRepository.java
│       │   │               │   ├── EstudioRepository.java
│       │   │               │   ├── EventoRepository.java
│       │   │               │   ├── FrasePersonalRepository.java
│       │   │               │   ├── FreelanceRepository.java
│       │   │               │   ├── GastoRepository.java
│       │   │               │   ├── HistorialPostergacionRepository.java
│       │   │               │   ├── IngresoRepository.java
│       │   │               │   ├── MateriaRepository.java
│       │   │               │   ├── MotivoEmocionalRepository.java
│       │   │               │   ├── ObservacionRIRepository.java
│       │   │               │   ├── PagoRepository.java
│       │   │               │   ├── PersonaRepository.java
│       │   │               │   ├── RegistroEmocionalRepository.java
│       │   │               │   ├── RegistroInvestigacionRepository.java
│       │   │               │   ├── SaludRepository.java
│       │   │               │   ├── SerieEventoRepository.java
│       │   │               │   ├── SocialRepository.java
│       │   │               │   ├── SyncChangeRepository.java
│       │   │               │   ├── SyncDeviceRepository.java
│       │   │               │   ├── SyncReceiptRepository.java
│       │   │               │   ├── TareaRepository.java
│       │   │               │   └── TrabajoRepository.java
│       │   │               ├── respaldo/
│       │   │               │   ├── ArchivoRespaldo.java
│       │   │               │   ├── CifradoRespaldo.java
│       │   │               │   ├── EsquemaRespaldo.java
│       │   │               │   ├── GestorRespaldo.java
│       │   │               │   └── ReglasCuentaRespaldo.java
│       │   │               ├── sync/
│       │   │               │   ├── AlmacenSync.java
│       │   │               │   ├── CambioSync.java
│       │   │               │   ├── CoordinadorSync.java
│       │   │               │   ├── IdentidadLocal.java
│       │   │               │   ├── JsonSync.java
│       │   │               │   ├── LocalSync.java
│       │   │               │   ├── MotorSync.java
│       │   │               │   ├── ReglasEstadoSync.java
│       │   │               │   ├── ReglasSync.java
│       │   │               │   ├── SesionSegura.java
│       │   │               │   ├── SesionSupabase.java
│       │   │               │   ├── SupabaseRest.java
│       │   │               │   ├── TrabajoSync.java
│       │   │               │   └── TransporteSync.java
│       │   │               ├── ui/
│       │   │               │   ├── ActividadTematica.java
│       │   │               │   ├── AgendaActivity.java
│       │   │               │   ├── AjustesActivity.java
│       │   │               │   ├── AmbienteInicioView.java
│       │   │               │   ├── AyudaActivity.java
│       │   │               │   ├── CatalogosActivity.java
│       │   │               │   ├── ConexionActivity.java
│       │   │               │   ├── Destinos.java
│       │   │               │   ├── EconomiaActivity.java
│       │   │               │   ├── EjercicioActivity.java
│       │   │               │   ├── EstadoEmocionalActivity.java
│       │   │               │   ├── EstiloApp.java
│       │   │               │   ├── EstudiosActivity.java
│       │   │               │   ├── EventosActivity.java
│       │   │               │   ├── FrasesActivity.java
│       │   │               │   ├── FreelanceActivity.java
│       │   │               │   ├── GraficoLineasView.java
│       │   │               │   ├── Iconos.java
│       │   │               │   ├── InvestigacionesActivity.java
│       │   │               │   ├── MenuNavegacion.java
│       │   │               │   ├── MovimientosActivity.java
│       │   │               │   ├── NotificacionesActivity.java
│       │   │               │   ├── PersonasActivity.java
│       │   │               │   ├── Postergaciones.java
│       │   │               │   ├── ProgresoCircularView.java
│       │   │               │   ├── ProgresoDia.java
│       │   │               │   ├── ProgresoInicio.java
│       │   │               │   ├── RadarView.java
│       │   │               │   ├── RegistroActivity.java
│       │   │               │   ├── RespaldoActivity.java
│       │   │               │   ├── ResumenDia.java
│       │   │               │   ├── SaludActivity.java
│       │   │               │   ├── SocialesActivity.java
│       │   │               │   ├── TareasActivity.java
│       │   │               │   ├── Tema.java
│       │   │               │   ├── TrabajoActivity.java
│       │   │               │   └── registro/
│       │   │               │       ├── Campo.java
│       │   │               │       ├── Definicion.java
│       │   │               │       ├── Fuente.java
│       │   │               │       ├── Fuentes.java
│       │   │               │       └── Registro.java
│       │   │               └── util/
│       │   │                   ├── Csv.java
│       │   │                   ├── JsonCursorHelper.java
│       │   │                   ├── ReglasAgenda.java
│       │   │                   └── UuidGenerator.java
│       │   └── res/
│       │       ├── drawable/
│       │       │   ├── bg_avatar.xml
│       │       │   ├── bg_button_primary.xml
│       │       │   ├── bg_card.xml
│       │       │   ├── bg_chip_selected.xml
│       │       │   ├── bg_chip_unselected.xml
│       │       │   ├── bg_fab.xml
│       │       │   ├── bg_field.xml
│       │       │   ├── bg_header.xml
│       │       │   ├── bg_menu_selected.xml
│       │       │   ├── bg_module.xml
│       │       │   ├── bg_quote.xml
│       │       │   ├── ic_aviso_agenda.xml
│       │       │   ├── ic_bell.xml
│       │       │   ├── ic_book.xml
│       │       │   ├── ic_briefcase.xml
│       │       │   ├── ic_calendar.xml
│       │       │   ├── ic_chevron.xml
│       │       │   ├── ic_coins.xml
│       │       │   ├── ic_dumbbell.xml
│       │       │   ├── ic_edit.xml
│       │       │   ├── ic_heart.xml
│       │       │   ├── ic_help.xml
│       │       │   ├── ic_home.xml
│       │       │   ├── ic_laptop.xml
│       │       │   ├── ic_launcher_background.xml
│       │       │   ├── ic_launcher_foreground.xml
│       │       │   ├── ic_leaf.xml
│       │       │   ├── ic_logo_plant.xml
│       │       │   ├── ic_menu.xml
│       │       │   ├── ic_more.xml
│       │       │   ├── ic_people.xml
│       │       │   ├── ic_plus.xml
│       │       │   ├── ic_quote.xml
│       │       │   ├── ic_research.xml
│       │       │   ├── ic_search.xml
│       │       │   ├── ic_settings.xml
│       │       │   ├── ic_square_agenda.xml
│       │       │   ├── ic_task.xml
│       │       │   └── progreso_inicio.xml
│       │       ├── drawable-nodpi/
│       │       │   ├── paisaje_bosque.png
│       │       │   ├── paisaje_bosque_night.png
│       │       │   ├── paisaje_espacial.png
│       │       │   └── paisaje_sunrise.png
│       │       ├── layout/
│       │       │   ├── activity_agenda.xml
│       │       │   ├── activity_ajustes.xml
│       │       │   ├── activity_ayuda.xml
│       │       │   ├── activity_catalogos.xml
│       │       │   ├── activity_conexion.xml
│       │       │   ├── activity_economia.xml
│       │       │   ├── activity_ejercicio.xml
│       │       │   ├── activity_estado_emocional.xml
│       │       │   ├── activity_estudios.xml
│       │       │   ├── activity_eventos.xml
│       │       │   ├── activity_frases.xml
│       │       │   ├── activity_freelance.xml
│       │       │   ├── activity_investigaciones.xml
│       │       │   ├── activity_main.xml
│       │       │   ├── activity_movimientos.xml
│       │       │   ├── activity_notificaciones.xml
│       │       │   ├── activity_personas.xml
│       │       │   ├── activity_respaldo.xml
│       │       │   ├── activity_salud.xml
│       │       │   ├── activity_sociales.xml
│       │       │   ├── activity_tareas.xml
│       │       │   ├── activity_trabajo.xml
│       │       │   ├── boton_accion.xml
│       │       │   ├── campo_casilla.xml
│       │       │   ├── campo_entrada.xml
│       │       │   ├── campo_selector.xml
│       │       │   ├── campos_recurrencia.xml
│       │       │   ├── celda_registro.xml
│       │       │   ├── detalle_registro.xml
│       │       │   ├── formulario_registro.xml
│       │       │   ├── item_acceso_inicio.xml
│       │       │   ├── item_agenda.xml
│       │       │   ├── item_menu_lateral.xml
│       │       │   ├── item_opcion_tema.xml
│       │       │   ├── item_registro.xml
│       │       │   ├── item_tarea_completa.xml
│       │       │   ├── item_tarea_inicio.xml
│       │       │   ├── layout_contenedor_navegacion.xml
│       │       │   ├── layout_menu_lateral.xml
│       │       │   └── layout_registros.xml
│       │       ├── mipmap-anydpi/
│       │       │   ├── ic_launcher.xml
│       │       │   └── ic_launcher_round.xml
│       │       ├── mipmap-hdpi/
│       │       │   ├── ic_launcher.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── mipmap-mdpi/
│       │       │   ├── ic_launcher.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── mipmap-xhdpi/
│       │       │   ├── ic_launcher.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── mipmap-xxhdpi/
│       │       │   ├── ic_launcher.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── mipmap-xxxhdpi/
│       │       │   ├── ic_launcher.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── values/
│       │       │   ├── colors.xml
│       │       │   ├── dimens.xml
│       │       │   ├── strings.xml
│       │       │   ├── styles.xml
│       │       │   ├── tema_attrs.xml
│       │       │   └── themes.xml
│       │       ├── values-night/
│       │       │   └── themes.xml
│       │       └── xml/
│       │           ├── backup_rules.xml
│       │           └── data_extraction_rules.xml
│       └── test/
│           └── java/
│               └── com/
│                   └── example/
│                       └── agendapersonal/
│                           ├── CsvTest.java
│                           ├── ExampleUnitTest.java
│                           ├── HoraAvisoTest.java
│                           ├── MotorSyncTest.java
│                           ├── ReglaRecurrenciaTest.java
│                           ├── ReglasAgendaTest.java
│                           ├── ReglasEstadoSyncTest.java
│                           ├── ReglasSyncTest.java
│                           ├── ResumenDiaTest.java
│                           └── respaldo/
│                               ├── ArchivoRespaldoTest.java
│                               └── ReglasCuentaRespaldoTest.java
├── build.gradle.kts
├── docs/
│   ├── ARBOLES_ARCHIVOS.md
│   ├── ARQUITECTURA.md
│   ├── BLUEPRINTS.md
│   ├── DATOS_Y_RESPALDOS.md
│   ├── DECISIONES_TECNICAS.md
│   ├── DICCIONARIO_DATOS.md
│   ├── ESTADO.md
│   ├── INSTALACION.md
│   ├── INVENTARIO_CODIGO.md
│   ├── MANTENIMIENTO.md
│   ├── PROTOCOLO_SYNC.md
│   ├── PRUEBAS.md
│   ├── PUBLICAR_EN_GITHUB.md
│   ├── REGLAS_FUNCIONALES.md
│   ├── SUPABASE.md
│   ├── blueprints/
│   │   ├── 01_contexto.mmd
│   │   ├── 02_componentes.mmd
│   │   ├── 03_envio.mmd
│   │   ├── 04_recuperacion.mmd
│   │   ├── 05_respaldo.mmd
│   │   └── 06_relaciones_datos.mmd
│   └── referencia/
│       └── esquema_sqlite.sql
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── gradlew.bat
├── migrations/
│   ├── 001_inicial_supabase.sql
│   ├── 002_historial_economia.sql
│   ├── 003_persistencia_sync.sql
│   └── 004_activar_retencion.sql
├── settings.gradle.kts
└── tools/
    ├── documentacion/
    │   └── generar_referencias.py
    └── pruebas-postgres/
        ├── package-lock.json
        ├── package.json
        └── test.mjs
```

Regenerar después de cambios de estructura: `python tools/documentacion/generar_referencias.py`.
