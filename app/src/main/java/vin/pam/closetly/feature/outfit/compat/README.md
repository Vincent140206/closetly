# Mix & Match integration compatibility

This package preserves the initial Mix & Match integration from `develop`.
Its classes and screens use a separate namespace so they can compile alongside
the complete implementation pushed to `feature/mix-and-match`.

`AppNavHost` uses the latest ViewModels, models, and screens outside `compat`.
The compatibility ViewModels and screens share the current repository models;
the original `imageUrl` model is retained under `compat.model`.
