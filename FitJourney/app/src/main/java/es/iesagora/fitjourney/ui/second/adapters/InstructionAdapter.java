package es.iesagora.fitjourney.ui.second.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.iesagora.fitjourney.R;

public class InstructionAdapter extends RecyclerView.Adapter<InstructionAdapter.InstructionViewHolder> {

    private List<String> instructions = new ArrayList<>();

    public void setInstructions(List<String> instructions) {
        this.instructions = instructions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InstructionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_instruction, parent, false);
        return new InstructionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InstructionViewHolder holder, int position) {
        String instruction = instructions.get(position);

        String stepNumber = String.format("%02d", position + 1);

        holder.tvStepNumber.setText(stepNumber);
        holder.tvStepText.setText(instruction);
    }

    @Override
    public int getItemCount() {
        return instructions != null ? instructions.size() : 0;
    }

    public static class InstructionViewHolder extends RecyclerView.ViewHolder {
        TextView tvStepNumber, tvStepText;

        public InstructionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStepNumber = itemView.findViewById(R.id.tvStepNumber);
            tvStepText = itemView.findViewById(R.id.tvStepText);
        }
    }
}