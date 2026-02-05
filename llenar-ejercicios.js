const https = require('https');
const fs = require('fs');

//URL donde estan los ejercicios
const url = 'https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/dist/exercises.json';

console.log("Inicio");

//Petición al repositorio
https.get(url, (respuesta) => {
    let datos = '';

    respuesta.on('data', (trozo) => {
        datos += trozo;
    });

    respuesta.on('end', () => {
        const listaEjercicios = JSON.parse(datos);
        
        console.log(`Descargados`);
        console.log("Generando el archivo");

        let archivoSQL = `INSERT INTO exercises (name, muscle_group, description, image_url, video_url, equipment, difficulty) VALUES \n`;

        const ejerciciosSeleccionados = listaEjercicios.filter(ejercicio => {
            return ['cardio', 'stretching', 'strength', 'plyometrics'].includes(ejercicio.category);
        }).slice(0, 80); 

        const valores = ejerciciosSeleccionados.map(ej => {

            //NOMBRE
            let nombre = ej.name.replace(/'/g, "''");
            nombre = nombre.charAt(0).toUpperCase() + nombre.slice(1);

            //GRUPO MUSCULAR
            let grupo = 'Cuerpo Completo';
            if (ej.category === 'cardio') grupo = 'Cardio';
            else if (ej.category === 'stretching') grupo = 'Flexibilidad';
            else if (ej.primaryMuscles.includes('chest')) grupo = 'Pecho';
            else if (ej.primaryMuscles.includes('back') || ej.primaryMuscles.includes('lats')) grupo = 'Espalda';
            else if (ej.primaryMuscles.includes('legs') || ej.primaryMuscles.includes('quadriceps') || ej.primaryMuscles.includes('hamstrings')) grupo = 'Piernas';
            else if (ej.primaryMuscles.includes('biceps') || ej.primaryMuscles.includes('triceps')) grupo = 'Brazos';
            else if (ej.primaryMuscles.includes('shoulders')) grupo = 'Hombros';
            else if (ej.primaryMuscles.includes('abdominals')) grupo = 'Abdominales';

            //DESCRIPCIÓN
            let descripcion = ej.instructions.join(' ');
            descripcion = descripcion.replace(/'/g, "''"); 

            //IMAGEN
            const imagen = `https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/${ej.images[0]}`;

            //VIDEO 
            const busqueda = (ej.name + " exercise").replace(/ /g, '+');
            const video = `https://www.youtube.com/results?search_query=${busqueda}`;

            //EQUIPAMIENTO
            let equipo = ej.equipment || 'Ninguno';
            if (equipo === 'body only') equipo = 'Peso Corporal';
            if (equipo === 'dumbbell') equipo = 'Mancuernas';
            if (equipo === 'barbell') equipo = 'Barra';

            //DIFICULTAD 
            let dificultad = ej.level || 'intermediate';
            if (dificultad === 'beginner') dificultad = 'Principiante';
            if (dificultad === 'intermediate') dificultad = 'Intermedio';
            if (dificultad === 'expert') dificultad = 'Experto';

            
            return `('${nombre}', '${grupo}', '${descripcion}', '${imagen}', '${video}', '${equipo}', '${dificultad}')`;
        });

        archivoSQL += valores.join(',\n') + ';';

        fs.writeFileSync('insertar_ejercicios.sql', archivoSQL);
        console.log("Archivo Creado");
    });

}).on("error", (err) => {
    console.log("Error al descargar: " + err.message);
});