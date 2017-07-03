package hardik124.silencio;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;

public class PlacesRV extends RecyclerView.Adapter<PlacesVH>{

    private ArrayList<places_model> mPlaces;

    public PlacesRV(ArrayList<places_model> mPlaces) {
        this.mPlaces = mPlaces;
    }

    @Override
    public PlacesVH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View personDetails = inflater.inflate(R.layout.places_row, parent, false);
        PlacesVH holder = new PlacesVH(personDetails);
        return holder;
    }
    @Override
    public void onBindViewHolder(PlacesVH holder, int position) {

        holder.setName(mPlaces.get(position).getName());
        holder.setAddress(mPlaces.get(position).getAddress());

    }

    @Override
    public int getItemCount() {
        return mPlaces.size();
    }
}

class PlacesVH extends RecyclerView.ViewHolder {

    public PlacesVH(View itemView) {
        super(itemView);
        itemView.setBackgroundColor(MaterialColorPalette.getRandomColor("700"));
    }
    public void setName(String name)
    {
        TextView nameTv= itemView.findViewById(R.id.place_name);
        nameTv.setText(name);
    }
    public void setAddress(String address)
    {
        TextView addrTv= itemView.findViewById(R.id.place_address);
        addrTv.setText(address);
    }
}