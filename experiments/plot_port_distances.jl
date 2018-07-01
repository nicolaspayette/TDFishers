using Gadfly, CSV, DataFrames

df = by(CSV.read("port_distances.csv"), [:step, :reward_delayed]) do df
  DataFrame(mean = mean(df[:distance]), std = std(df[:distance]))
end
p = plot(df,
  x = :step, Guide.xlabel("Time step"),
  y = :mean, Guide.ylabel("Average towing distance from port", orientation=:vertical),
  ymin = df[:mean] - df[:std],
  ymax = df[:mean] + df[:std],
  color = :reward_delayed, Guide.colorkey(title="Reward delayed?"),
  Geom.line, Geom.ribbon)
Gadfly.with_theme(:default) do
  draw(PNG("port_distances.png", 180, 120), p)
end
