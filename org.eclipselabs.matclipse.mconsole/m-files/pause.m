function pause(varargin)

if nargin==1 && ~ischar(varargin{1})
    if varargin{1} > 2.0, 
        varargin{1} = 2.0;
        fprintf(1,'%s\n','Pause Mode: Automatically terminated after 2 seconds.')
    end
    builtin('pause',varargin{:});
else
    fprintf(1,'%s\n','Pause Mode: Automatically terminated after 2 seconds.')
    builtin('pause',2);
end