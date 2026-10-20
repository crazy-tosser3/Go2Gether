package com.crzto3.go2gether.ui.map

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.crzto3.go2gether.config.AppConfig
import com.crzto3.go2gether.data.ApiClient
import com.crzto3.go2gether.ui.theme.Radius
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class GeoPoint(val lat: Double, val lng: Double)

data class MapMarker(val id: Long, val point: GeoPoint)

/**
 * Веб-карта 2ГИС (MapGL JS) в системном WebView своей платформы.
 * Страница шлёт события строкой через [onMessage]: `marker:<id>` — нажали метку, `point:<lat>,<lng>` — выбрали точку.
 */
@Composable
expect fun PlatformWebMap(html: String, modifier: Modifier, onMessage: (String) -> Unit)

@Serializable
private data class MapConfig(
    val key: String,
    val center: List<Double>,
    val zoom: Double,
    val markers: List<List<Double>>,
    val fit: Boolean,
    val picker: Boolean,
    val point: List<Double>?,
)

private val ConfigJson = Json { explicitNulls = true }

/** Страница карты: метки, вписывание в экран и, для формы, выбор точки нажатием. */
internal fun mapHtml(
    markers: List<MapMarker>,
    center: GeoPoint?,
    zoom: Double,
    picker: Boolean,
    point: GeoPoint?,
): String {
    val c = center ?: point ?: markers.firstOrNull()?.point ?: GeoPoint(AppConfig.DEFAULT_LAT, AppConfig.DEFAULT_LNG)
    val config = MapConfig(
        key = AppConfig.TWO_GIS_KEY,
        center = listOf(c.lng, c.lat),
        zoom = zoom,
        // Порядок как у MapGL: [долгота, широта]; третьим — id мероприятия.
        markers = markers.map { listOf(it.point.lng, it.point.lat, it.id.toDouble()) },
        fit = center == null && markers.size > 1,
        picker = picker,
        point = point?.let { listOf(it.lng, it.lat) },
    )
    return """
<!doctype html>
<html><head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<style>
html,body,#map{margin:0;padding:0;width:100%;height:100%;background:#eeeff4;font:15px -apple-system,system-ui,sans-serif;color:#4a4d5c}
#err{display:none;position:absolute;inset:0;align-items:center;justify-content:center;text-align:center;padding:16px}
</style>
<script src="${AppConfig.MAPGL_SCRIPT}"></script>
</head><body>
<div id="map"></div><div id="err">Карта не загрузилась. Проверьте интернет.</div>
<script>
function send(m){try{if(window.Android){Android.post(m)}else{window.webkit.messageHandlers.app.postMessage(m)}}catch(e){}}
(function(){
  var cfg=${ConfigJson.encodeToString(MapConfig.serializer(), config)};
  if(!window.mapgl){document.getElementById('err').style.display='flex';return}
  var map=new mapgl.Map('map',{center:cfg.center,zoom:cfg.zoom,key:cfg.key,zoomControl:'topRight'});
  cfg.markers.forEach(function(m){
    var mk=new mapgl.Marker(map,{coordinates:[m[0],m[1]]});
    mk.on('click',function(){send('marker:'+m[2])});
  });
  if(cfg.fit){
    var lngs=cfg.markers.map(function(m){return m[0]}),lats=cfg.markers.map(function(m){return m[1]});
    map.fitBounds({southWest:[Math.min.apply(null,lngs),Math.min.apply(null,lats)],northEast:[Math.max.apply(null,lngs),Math.max.apply(null,lats)]},{padding:{top:48,bottom:48,left:48,right:48}});
  }
  if(cfg.picker){
    var pick=cfg.point?new mapgl.Marker(map,{coordinates:cfg.point}):null;
    map.on('click',function(e){
      if(pick)pick.destroy();
      pick=new mapgl.Marker(map,{coordinates:e.lngLat});
      send('point:'+e.lngLat[1]+','+e.lngLat[0]);
    });
  }
})();
</script>
</body></html>
""".trimIndent()
}

/** Карта с метками мероприятий; нажатие на метку — [onMarker]. */
@Composable
fun EventsMap(
    markers: List<MapMarker>,
    modifier: Modifier = Modifier,
    height: Dp = 260.dp,
    center: GeoPoint? = null,
    zoom: Double = 13.0,
    onMarker: (Long) -> Unit = {},
) {
    val html = remember(markers, center, zoom) { mapHtml(markers, center, zoom, picker = false, point = null) }
    val click by rememberUpdatedState(onMarker)
    PlatformWebMap(html, modifier.fillMaxWidth().height(height).clip(Radius.lg)) { msg ->
        msg.removePrefix("marker:").takeIf { msg.startsWith("marker:") }?.toDoubleOrNull()?.let { click(it.toLong()) }
    }
}

/**
 * Выбор точки встречи нажатием на карту — как LocationPicker. [version] меняют, когда точку задали снаружи
 * (поиск по адресу): страница перерисуется с новым центром.
 */
@Composable
fun LocationPicker(point: GeoPoint?, version: Int, modifier: Modifier = Modifier, onPick: (GeoPoint) -> Unit) {
    // Нажатие на карту страница рисует сама — перезагружать её на каждую точку не нужно.
    val html = remember(version) { mapHtml(emptyList(), center = null, zoom = if (point != null) 16.0 else 12.0, picker = true, point = point) }
    val pick by rememberUpdatedState(onPick)
    PlatformWebMap(html, modifier.fillMaxWidth().height(280.dp).clip(Radius.lg)) { msg ->
        if (!msg.startsWith("point:")) return@PlatformWebMap
        val (lat, lng) = msg.removePrefix("point:").split(',').mapNotNull { it.toDoubleOrNull() }.takeIf { it.size == 2 } ?: return@PlatformWebMap
        pick(GeoPoint(lat, lng))
    }
}

// Город добавляем, чтобы «Центральный парк» искался в нужном городе (features/map/geocode.ts).
private fun geocodeQuery(address: String, city: String): String {
    val a = address.trim()
    val c = city.trim()
    return if (c.isNotEmpty() && !a.lowercase().contains(c.lowercase())) "$c, $a" else a
}

/** Точка по адресу через геокодер 2ГИС; null — не нашли. Бросает исключение, если геокодер недоступен. */
suspend fun geocode(client: ApiClient, address: String, city: String): GeoPoint? {
    val response = client.http.get(AppConfig.GEOCODER_URL) {
        parameter("q", geocodeQuery(address, city))
        parameter("fields", "items.point")
        parameter("key", AppConfig.TWO_GIS_KEY)
    }
    if (!response.status.isSuccess()) error("geocode ${response.status.value}")
    val items = Json.parseToJsonElement(response.bodyAsText()).jsonObject["result"]?.jsonObject?.get("items")?.jsonArray
        ?: return null
    val point = items.firstNotNullOfOrNull { it.jsonObject["point"]?.jsonObject } ?: return null
    return GeoPoint(point["lat"]!!.jsonPrimitive.double, point["lon"]!!.jsonPrimitive.double)
}
