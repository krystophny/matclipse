/*******************************************************************************
 * Copyright (c) 2006, 2011 Institute of Theoretical and Computational Physics (ITPCP), 
 * Graz University of Technology.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Georg Huhs, Winfried Kernbichler, David Camhy (ITPCP) - 
 *         initial API and implementation
 * Last changed: 
 *     2008-01-23
 *******************************************************************************/

package org.eclipselabs.matclipse.meditor.editors.partitioner;


import org.eclipse.jface.text.rules.IWordDetector;


class GreatKeywordDetector implements IWordDetector {
        
    static public String[] keywords = {
            "break", "case", "catch", "continue", "else", "elseif", 
//                "for", "function", "global", "if", "otherwise", 
            "end", "for", "function", "global", "if", "otherwise", 
            "persistent", "return", "switch", "try", "while"
    };

    static public String[] functions = {
        "abs", "acos", "acosh", "acot" ,"acoth", "acsc", "acsch",
        "airfoil" ,"all" ,"andrew" ,"angle", "angle" ,"ans" ,"any", "arith",
        "asec", "asech", "asin", "asinh", "atan", "atan2", "atanh", "auread",
        "auwrite", "axes", "axis", "balance", "bar","bartlett", "bench", "bessel",
        "bessela", "besselap", "besself", "besseli", "besselj", "besselk", "bessely", "beta",
        "betacore", "betainc", "betaln", "bilinear", "blackman", "blanks", "blt", "bone",
        "boxcar", "brighten", "bucky", "buttap", "butter", "buttonv", "buttord", 
        "cart2pol", "cart2sph", "caxis", "cceps", "cd", "cdf2rdf", "cedit", "ceil",
        "census", "censusex", "cheb1ap", "cheb1ord", "cheb2ap", "cheb2ord", "chebwin", 
        "cheby1", "cheby2", "choices", "choicex", "chol", "cinvert", "cla", "clabel", "clc",
        "clear", "clf", "clg", "clock", "close", "cohere", "colmmd", "colon",
        "colormap", "colormenu", "colperm", "colstyle", "comet", "comet3", "compan", "compass",
        "computer", "cond", "condest", "conj", "contour", "contour3", "contourc", "contrast",
        "conv", "conv2", "conv2", "conv2", "convmtx", "cool", "copper", "corrcoef",
        "corrcoef", "cos", "cosh", "cot", "coth", "cov", "cov", "cplxdemo",
        "cplxgrid", "cplxmap", "cplxpair", "cplxpair", "cplxroot", "cputime","cross","csc",
        "csch", "csd","cumprod", "cumsum", "cylinder", "czt", "cztdemo", "d",
        "datalist", "date", "dbclear", "dbcont", "dbdown", "dbquit", "dbstack", "dbstatus",
        "dbstep", "dbstop", "dbtype", "dbup", "dc2sc", "dct", "deblank", "debug",
        "dec2hex", "decimate", "deconv", "deconv", "del2", "delete", "delsq", "delsqdemo",
        "delsqshow", "demo", "demod", "det", "detrend", "dftmtx", "diag", "diary",
        "diff", "dir", "diric", "disp", "dmperm", "dos", "drawnow", "earthex",
        "earthmap", "echo", "eig", "eigmovie", "ellip", "ellipap", "ellipj", "ellipk",
        "ellipke", "ellipord", "eps", "erf", "erfc",
        "erfcore", "erfcx", "erfinv", "error", "errorbar", "etime", "etree", "etreeplot",
        "eval", "exist", "exp", "expm", "expm1", "expm2", "expm3", "eye",
        "fclose", "feather", "feof", "ferror", "feval", "fft", "fft", "fft2",
        "fft2", "fftdemo", "fftfilt", "fftshift", "fftshift", "fftshift", "fgetl", "fgets",
        "figtext", "figure", "fill", "fill3", "filtdemo", "filter", "filter", "filter2",
        "filtfilt", "filtic", "find", "findstr", "finite", "fir1", "fir2", "firls",
        "fitdemo", "fitfun", "fix", "flag", "fliplr", "flipud", "floor", "flops",
        "fmin", "fmins", "fopen", "foptions", "format", "fourier2", "fplot",
        "fplotdemo", "fprintf", "fread", "freqs", "freqspace", "freqz", "frewind", "fscanf",
        "fseek", "ftell", "full", "funm", "fwrite", "fzero", "gallery",
        "gamma", "gammainc", "gammaln", "gca", "gcd", "gcf", "get", "getenv",
        "getframe", "ginput", "gplot", "gradient", "gray", "graymon", "grid",
        "griddata", "grpdelay", "gtext", "hadamard", "hamming", "hankel", "hanning", "hardcopy",
        "help", "hess", "hex2dec", "hex2num", "hidden", "highlight", "hilb", "hilbert",
        "hint", "hist", "hold", "home", "hostid", "hot", "hsv", "hsv2rgb", 
        "humps", "i", "icubic", "idct", "ident", "ifft", "ifft",
        "ifft2", "ifft2", "iffuse", "imag", "image", "imagedemo", "imageext", "imagesc",
        "impinvar", "impz", "imread", "imtext", "imwrite", "inf", "info", "input",
        "inquire", "int2str", "interp", "interp1", "interp1", "interp2", "interp3", "interp4",
        "interp5", "interpft", "intfilt", "intro", "inv", "inverf", "invfreqs", "invfreqz",
        "invhilb", "isempty", "isglobal", "ishold", "isieee", "isinf", "isletter", "isnan",
        "issparse", "isstr", "isunix", "j", "jet", "kaiser", "keyboard", "knot",
        "kron", "lalala", "lasterr", "lcm", "legend", "length", "levinson", "life", "lifeloop2",
        "lin2mu", "line", "linspace", "load", "loadwave", "log", "log10", "log2",
        "loglog", "logm", "logspace", "lookfor", "lorenz", "lorenzeq", "lotka", "lower",
        "lp2bp", "lp2bs", "lp2hp", "lp2lp", "lpc", "ls", "lscov", "lu",
        "magic","man", "mathlist", "matlabro", "max", "mean", "medfilt1", "median",
        "membrane", "memory", "menu", "mesh", "meshc", "meshdom", "meshgrid", "meshz",
        "meta", "min", "mkpp", "mmove2", "moddemo", "modulate", "more", "movie",
        "moviein", "mu2lin", "nalist", "nan", "nargchk", "nargin", "nargout", "nestdiss",
        "nested", "newplot", "nextpow2", "nnls", "nnz", "nonzeros", "norm", "normest",
        "null", "num2str", "numgrid", "nzmax", "ode23", "ode23p", "ode45", "odedemo",
        "ones", "orient", "orth", "pack", "paren", "pascal", "patch", "path",
        "pause", "pcolor", "peaks", "penny", "pi", "pink", "pinv", "planerot",
        "plot", "plot3", "pol2cart", "polar", "poly", "poly2rc", "polyder", "polyfit",
        "polyline", "polymark", "polystab", "polyval", "polyvalm", "pow2", "ppval", "print",
        "printopt", "prism", "prod", "prony", "psd", "punct", "puzzle", "pwd",
        "qr", "qrdelete", "qrinsert", "quad", "quad8", "quad8stp", "quaddemo", "quadstp",
        "quake", "quit", "quiver", "qz", "rand", "randn", "randperm", "rank",
        "rat", "rats", "rbbox", "rc2poly", "rceps", "rcond", "readme", "real",
        "realmax", "realmin", "relop2", "rem", "remez", "remezord", "resample", "reset",
        "reshape", "resi2", "residue", "residuez", "rgb2hsv", "rgbplot", "rjr",
        "roots", "rose", "rosser", "rot90", "round", "rref", "rrefmovie", "rsf2csf",
        "save", "savewave", "sawtooth", "saxis", "sc2dc", "schur", "script", "sec",
        "sech", "semilogx", "semilogy", "sepdemo", "sepplot", "set", "setstr", "shading",
        "shg", "showwind", "sig1help","sig2help", "sigdemo1", "sigdemo2", "sign","sin",
        "sinc", "sinh", "size", "slash", "slice", "sort", "sos2ss", "sos2tf",
        "sos2zp", "sound", "sounddemo", "soundext", "spalloc", "sparlist", "sparse", "sparsfun",
        "sparsity", "spaugment", "spconvert", "spdiags", "specgram", "specials", "spectrum", "specular",
        "speye", "spfun", "sph2cart", "sphere", "spinmap", "spiral", "spline", "spline",
        "spline2d", "spones", "spparms", "sprandn", "sprandsym", "sprank", "sprintf", "spy",
        "spypart", "sqdemo2", "sqrt", "sqrtm", "square", "ss2sos", "ss2tf", "ss2zp",
        "sscanf", "stairs", "std", "stem", "stem", "stmcb", "str2mat", "str2num",
        "strcmp", "strings", "strips", "subplot", "subscribe", "subspace", "sum", "sunspots",
        "superquad", "surf", "surface", "surfc", "surfl", "surfnorm", "svd", "swapprev",
        "symbfact", "symmmd", "symrcm", "table1", "table2", "tan", "tanh", "tempdir",
        "tempname", "terminal", "text", "tf2ss", "tf2zp", "tfe", "tffunc", "tic",
        "title", "toc", "toeplitz", "trace", "trapz", "treelayout", "treeplot", "triang",
        "tril", "triu", "type", "uicontrol", "uigetfile", "uimenu", "uiputfile", "uisetcolor",
        "uisetfont", "unix", "unmesh", "unmkpp", "unwrap", "unwrap", "upper", "vander",
        "vco", "ver", "version", "vibes", "view", "viewmtx", "waterfall", "what",
        "whatsnew", "which", "white", "whitebg", "who", "whos", "why",
        "wilkinson", "xcorr", "xcorr2", "xcov", "xlabel", "xor", "xyzchk", "ylabel",
        "yulewalk", "zerodemo", "zeros", "zlabel", "zp2sos", "zp2ss", "zp2tf", "zplane" 
    };


    public GreatKeywordDetector() {
    }
    
    
    public boolean isWordStart(char c) {
        return Character.isJavaIdentifierStart(c);
    }
    
    
    public boolean isWordPart(char c) {
        return Character.isJavaIdentifierPart(c);
    }

}
