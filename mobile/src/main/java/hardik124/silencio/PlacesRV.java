package hardik124.silencio;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;

import hardik124.silencio.database.DB_Contract;

public class PlacesRV extends RecyclerView.Adapter<PlacesRV.PlacesVH>{

    private ArrayList<places_model> mPlaces = new ArrayList<>();
    private Context mContext ;
    public PlacesRV(ArrayList<places_model> mPlaces,Context mContext) {
        this.mPlaces = mPlaces;
        this.mContext = mContext;
    }

    @Override
    public PlacesVH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View personDetails = inflater.inflate(R.layout.places_row, parent, false);
        PlacesVH holder = new PlacesVH(personDetails);

        return holder;
    }
    @Override
    public void onBindViewHolder(PlacesVH holder, int position) {

        places_model place = mPlaces.get(position);
        holder.setName(place.getName());
        holder.setAddress(place.getAddress());
        holder.setDelBtn(place.getKey(),position);
    }

    @Override
    public int getItemCount() {
        return mPlaces.size();
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
        public void setDelBtn(final String id,final int pos)
        {
            AppCompatImageView del = itemView.findViewById(R.id.dlt_btn);
            del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPlaces.remove(pos);
                    mContext.getContentResolver().delete(DB_Contract.PlacesTable.CONTENT_URI,
                            DB_Contract.PlacesTable.COLOUMN_PLACE_ID,
                            new String[]{id});
                    ((Home)mContext).refreshPlacesData();
                    notifyDataSetChanged();
                }
            });
        }
    }
}

