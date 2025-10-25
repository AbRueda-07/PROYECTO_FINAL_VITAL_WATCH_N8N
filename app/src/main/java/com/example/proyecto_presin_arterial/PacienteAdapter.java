package com.example.proyecto_presin_arterial;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class PacienteAdapter extends RecyclerView.Adapter<PacienteAdapter.PacienteViewHolder> {
    private List<Map<String, String>> pacientesList;
    private OnPacienteClickListener listener;

    public interface OnPacienteClickListener {
        void onPacienteClick(Map<String, String> paciente);
    }

    public PacienteAdapter(List<Map<String, String>> pacientesList, OnPacienteClickListener listener) {
        this.pacientesList = pacientesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PacienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_paciente, parent, false);
        return new PacienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PacienteViewHolder holder, int position) {
        Map<String, String> paciente = pacientesList.get(position);

        holder.tvNombre.setText("👤 " + paciente.get("nombre"));
        holder.tvEdad.setText("Edad: " + paciente.get("edad"));
        holder.tvGenero.setText("Género: " + paciente.get("genero"));
        holder.tvContacto.setText("📞 " + paciente.get("contacto"));
        holder.tvEnfermedades.setText("🏥 Enfermedades: " + paciente.get("enfermedades"));
        holder.tvMedicamentos.setText("💊 Medicamentos: " + paciente.get("medicamentos"));
        holder.tvPresion.setText("📊 Presión: " + paciente.get("presion") + " (" + paciente.get("categoria_presion") + ")");    }

    @Override
    public int getItemCount() {
        return pacientesList.size();
    }

    public void updateList(List<Map<String, String>> newList) {
        this.pacientesList = newList;
        notifyDataSetChanged();
    }

    class PacienteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvEdad, tvGenero, tvContacto, tvEnfermedades, tvMedicamentos, tvPresion;

        public PacienteViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNombre = itemView.findViewById(R.id.tvNombrePaciente);
            tvEdad = itemView.findViewById(R.id.tvEdadPaciente);
            tvGenero = itemView.findViewById(R.id.tvGeneroPaciente);
            tvContacto = itemView.findViewById(R.id.tvContactoPaciente);
            tvEnfermedades = itemView.findViewById(R.id.tvEnfermedadesPaciente);
            tvMedicamentos = itemView.findViewById(R.id.tvMedicamentosPaciente);
            tvPresion = itemView.findViewById(R.id.tvPresionPaciente);

            // Listener para clic en el paciente
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onPacienteClick(pacientesList.get(position));
                    }
                }
            });
        }
    }
}