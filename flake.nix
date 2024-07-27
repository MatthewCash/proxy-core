{
    description = "Proxy Core";

    inputs = {
        nixpkgs.url = "nixpkgs/nixos-unstable";

        flake-utils = {
            url = "github:numtide/flake-utils";
            inputs.nixpkgs.follows = "nixpkgs";
        };
    };

    outputs = { nixpkgs, flake-utils, ... }:
    flake-utils.lib.eachDefaultSystem (system:
        let pkgs = nixpkgs.legacyPackages.${system}; in {
            devShell = pkgs.mkShell {
                name = "proxy-core";
                packages = with pkgs; [
                    gradle
                ];
            };
        }
    );
}
