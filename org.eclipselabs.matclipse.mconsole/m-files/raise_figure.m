function raise_figure(in)
% Holt Figures in den Vordergrund.
% Aufruf: raise_figure('all') - alle Figures
%         raise_figure(h) - h ist Handle oder Vektor von Handles
%         raise_figure(tag) - tag for findobj
%
% Winfried Kernbichler 15.02.2006
if nargin < 1, return; end

if ischar(in)
    if strcmp(in,'all')
        figs = get(0,'children');
    else
        figs = findobj(0,'Tag',in);
        if isempty(figs), return; end
    end
elseif any(ishandle(in))
    figs = intersect(get(0,'children'),in(:));
else
    return
end

for k = numel(figs):-1:1
    if strcmp(get(figs(k),'Type'),'figure'), figure(figs(k)); end
end
        
