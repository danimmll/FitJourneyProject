package es.iesagora.fitjourney.ui.second.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.iesagora.fitjourney.R;

/**
 * Adapter encargado de mostrar una lista de instrucciones paso a paso
 * dentro del detalle de un ejercicio.
 *
 * <p>Cada instrucción se representa con:</p>
 * <ul>
 *     <li>Número de paso formateado (01, 02, 03...).</li>
 *     <li>Texto descriptivo del paso.</li>
 * </ul>
 *
 * <p>Se utiliza en el {@code ExerciseDetailFragment} para descomponer
 * la descripción del ejercicio en pasos visuales más claros.</p>
 */
public class InstructionAdapter extends RecyclerView.Adapter<InstructionAdapter.InstructionViewHolder> {

    /** Lista de instrucciones del ejercicio */
    private List<String> instructions = new ArrayList<>();

    /**
     * Actualiza la lista de instrucciones mostradas.
     *
     * @param instructions nueva lista de pasos del ejercicio
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setInstructions(List<String> instructions) {
        this.instructions = instructions;
        notifyDataSetChanged();
    }

    /**
     * Infla la vista de cada instrucción.
     *
     * @param parent contenedor padre
     * @param viewType tipo de vista
     * @return ViewHolder de la instrucción
     */
    @NonNull
    @Override
    public InstructionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewholder_instruction, parent, false);
        return new InstructionViewHolder(view);
    }

    /**
     * Vincula cada instrucción con su vista.
     *
     * <p>Se encarga de mostrar el número de paso y el texto correspondiente.</p>
     *
     * @param holder ViewHolder de la instrucción
     * @param position posición en la lista
     */
    @Override
    public void onBindViewHolder(@NonNull InstructionViewHolder holder, int position) {

        String instruction = instructions.get(position);

        @SuppressLint("DefaultLocale") String stepNumber = String.format("%02d", position + 1);

        holder.tvStepNumber.setText(stepNumber);
        holder.tvStepText.setText(instruction);
    }

    /**
     * Devuelve el número total de instrucciones.
     *
     * @return tamaño de la lista de instrucciones
     */
    @Override
    public int getItemCount() {
        return instructions != null ? instructions.size() : 0;
    }

    /**
     * ViewHolder que representa cada paso de instrucción.
     */
    public static class InstructionViewHolder extends RecyclerView.ViewHolder {

        /** Número del paso (01, 02, 03...) */
        TextView tvStepNumber;

        /** Texto descriptivo del paso */
        TextView tvStepText;

        /**
         * Inicializa las vistas del item de instrucción.
         *
         * @param itemView vista inflada del layout
         */
        public InstructionViewHolder(@NonNull View itemView) {
            super(itemView);

            tvStepNumber = itemView.findViewById(R.id.tvStepNumber);
            tvStepText = itemView.findViewById(R.id.tvStepText);
        }
    }
}